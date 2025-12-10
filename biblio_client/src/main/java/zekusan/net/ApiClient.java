package zekusan.net;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;

import zekusan.enums.ActionType;
import zekusan.enums.ItemType;
import zekusan.enums.Status;
import zekusan.comms.requests.CatalogoRequest;
import zekusan.comms.requests.LoginRequest;
import zekusan.comms.requests.PrenotazioneRequest;
import zekusan.comms.responses.CatalogoResponse;
import zekusan.comms.responses.LoginResponse;
import zekusan.comms.responses.PrenotazioneResponse;
import zekusan.comms.responses.Response;
import zekusan.models.items.Item;
import zekusan.models.items.CD;
import zekusan.models.items.Libro;
import zekusan.models.items.Rivista;
import zekusan.models.loans.LoanInfo;
import zekusan.models.loans.PendingLoanInfo;

public class ApiClient {
	private final SocketClient socketClient;
	private final List<LoanInfo> mockLoaned = new ArrayList<>();
	private final List<PendingLoanInfo> mockPending = new ArrayList<>();
	private final List<Item> mockCatalog = new ArrayList<>();
	private int nextLoanId = 1000;
	private int nextRequestId = 2000;
	private int nextCatalogId = 3000;

	public ApiClient(SocketClient socketClient) {
		this.socketClient = socketClient;
		seedMockLoans();
	}

	public LoginResponse login(String username, String password) throws IOException {
		LoginRequest request = new LoginRequest(username, password);
		return send(ActionType.LOGIN, request, LoginResponse.class);
	}

	public CatalogoResponse fetchCatalogo(int token, String username, ItemType categoria) throws IOException {
		CatalogoRequest request = new CatalogoRequest(token, username, categoria);
		CatalogoResponse response = sendCatalogo(request);
		response.setCatalogo(mergeWithMockCatalog(response.getCatalogo(), categoria));
		return response;
	}

	public PrenotazioneResponse prenota(int token, String username, int itemId, ItemType type) throws IOException {
		PrenotazioneRequest request = new PrenotazioneRequest(itemId, type);
		request.setToken(token);
		request.setUsername(username);
		return send(ActionType.PRENOTAZIONE, request, PrenotazioneResponse.class);
	}

	public synchronized List<LoanInfo> fetchLoanedItems(int token, String username) {
		return mockLoaned.stream()
				.map(ApiClient::copyLoan)
				.toList();
	}

	public synchronized List<PendingLoanInfo> fetchPendingLoans(int token, String username) {
		return mockPending.stream()
				.map(ApiClient::copyPending)
				.toList();
	}

	public synchronized List<LoanInfo> fetchUserLoanedItems(int token, String username) {
		ensureUserSamples(username);
		return mockLoaned.stream()
				.filter(loan -> username != null && username.equalsIgnoreCase(loan.getBorrower()))
				.map(ApiClient::copyLoan)
				.toList();
	}

	public synchronized List<PendingLoanInfo> fetchUserPendingLoans(int token, String username) {
		ensureUserSamples(username);
		return mockPending.stream()
				.filter(p -> username != null && username.equalsIgnoreCase(p.getBorrower()))
				.map(ApiClient::copyPending)
				.toList();
	}

	public synchronized LoanInfo acceptPendingLoan(int token, String username, int requestId) throws IOException {
		PendingLoanInfo pending = mockPending.stream()
				.filter(req -> req.getId() == requestId)
				.findFirst()
				.orElse(null);

		if (pending == null) {
			throw new IOException("Richiesta non trovata.");
		}

		mockPending.remove(pending);

		LocalDate baseDate = pending.getRequestedOn() != null ? pending.getRequestedOn() : LocalDate.now();
		LoanInfo loan = new LoanInfo(
				nextLoanId(),
				pending.getItemId(),
				pending.getItemName(),
				pending.getCategory(),
				pending.getBorrower(),
				pending.getRequestedOn(),
				baseDate.plusDays(14));

		mockLoaned.add(loan);
		return copyLoan(loan);
	}

	public synchronized boolean cancelPendingLoan(int token, String username, int requestId) {
		return mockPending.removeIf(req -> req.getId() == requestId);
	}

	public synchronized boolean returnLoan(int token, String username, int loanId) {
		return mockLoaned.removeIf(loan ->
				loan.getId() == loanId && username != null && username.equalsIgnoreCase(loan.getBorrower()));
	}

	public synchronized boolean cancelPendingBorrow(int token, String username, int requestId) {
		return mockPending.removeIf(req ->
				req.getId() == requestId && username != null && username.equalsIgnoreCase(req.getBorrower()));
	}

	public synchronized Item addItem(int token, String username, Item item) throws IOException {
		Item toStore = copyItem(item);
		if (toStore == null) {
			throw new IOException("Item non valido");
		}
		if (toStore.getId() <= 0) {
			toStore.setId(nextCatalogItemId());
		}
		ensureItemType(toStore);
		mockCatalog.removeIf(existing -> existing.getId() == toStore.getId());
		mockCatalog.add(copyItem(toStore));
		logMockAction("Aggiunta", username, toStore);
		return copyItem(toStore);
	}

	public synchronized Item updateItem(int token, String username, Item item) throws IOException {
		Item updated = copyItem(item);
		if (updated == null) {
			throw new IOException("Item non valido");
		}
		if (updated.getId() <= 0) {
			updated.setId(nextCatalogItemId());
		}
		ensureItemType(updated);
		mockCatalog.removeIf(existing -> existing.getId() == updated.getId());
		mockCatalog.add(copyItem(updated));
		logMockAction("Aggiornamento", username, updated);
		return copyItem(updated);
	}

	private CatalogoResponse sendCatalogo(CatalogoRequest request) throws IOException {
		String payload = formatPayload(ActionType.CATALOGO, Converter.objectToJson(request));
		String responseText = socketClient.send(payload);
		ParsedBody body = parseBody(responseText);

		if (body.action != ActionType.CATALOGO) {
			throw new IOException("Unexpected action in response: " + body.action);
		}

		try {
			return parseCatalogoResponse(body.body);
		} catch (JacksonException e) {
			throw new IOException("Unable to parse catalog response", e);
		}
	}

	private <T extends Response> T send(ActionType action, Object request, Class<T> responseType) throws IOException {
		String payload = formatPayload(action, Converter.objectToJson(request));
		String responseText = socketClient.send(payload);
		ParsedBody body = parseBody(responseText);

		if (body.action != action) {
			throw new IOException("Unexpected action in response: " + body.action);
		}

		try {
			return responseType.cast(Converter.jsonToResponse(body.body, action));
		} catch (JacksonException e) {
			throw new IOException("Unable to parse response", e);
		}
	}

	private CatalogoResponse parseCatalogoResponse(String body) throws JacksonException {
		JsonNode root = Converter.mapper().readTree(body);

		CatalogoResponse response = new CatalogoResponse();

		JsonNode statusNode = root.path("status");
		if (!statusNode.isMissingNode()) {
			try {
				response.setStatus(Status.valueOf(statusNode.asText()));
			} catch (IllegalArgumentException e) {
				response.setStatus(Status.NONE);
			}
		}

		ItemType categoria = ItemType.NONE;
		JsonNode categoriaNode = root.path("categoria");
		if (!categoriaNode.isMissingNode()) {
			try {
				categoria = ItemType.valueOf(categoriaNode.asText());
			} catch (IllegalArgumentException ignored) {
				categoria = ItemType.NONE;
			}
		}
		response.setCategoria(categoria);

		List<Item> items = new ArrayList<>();
		JsonNode catalogoNode = root.path("catalogo");
		if (catalogoNode.isArray()) {
			for (JsonNode node : catalogoNode) {
				try {
					Item item = categoria == ItemType.NONE
							? Converter.mapper().readValue(node.toString(), Item.class)
							: Converter.jsonToItem(node.toString(), categoria);
					items.add(item);
				} catch (JacksonException e) {
					// skip malformed item but continue processing
				}
			}
		}
		response.setCatalogo(items);

		return response;
	}

	private ParsedBody parseBody(String raw) throws IOException {
		if (raw == null || raw.isBlank()) {
			throw new IOException("Empty response from server");
		}

		int separatorIndex = raw.indexOf(SEPARATOR);
		if (separatorIndex <= 0) {
			throw new IOException("Malformed response: no separator found");
		}

		String header = raw.substring(0, separatorIndex);
		String body = raw.substring(separatorIndex + 1);
		ActionType action = ActionType.getType(header);
		if (action == ActionType.NONE) {
			throw new IOException("Unknown action in response: " + header);
		}

		return new ParsedBody(action, body);
	}

	private String formatPayload(ActionType action, String body) {
		return headerFor(action) + SEPARATOR + body;
	}

	private String headerFor(ActionType action) {
		return action.name().toLowerCase();
	}

	private record ParsedBody(ActionType action, String body) {
	}

	private static final char SEPARATOR = '|';

	private List<Item> mergeWithMockCatalog(List<Item> catalogo, ItemType categoria) {
		List<Item> result = new ArrayList<>();
		if (catalogo != null) {
			for (Item item : catalogo) {
				Item copy = copyItem(item);
				if (copy != null) {
					result.add(copy);
				}
			}
		}

		ItemType filter = categoria == null ? ItemType.NONE : categoria;
		synchronized (this) {
			for (Item item : mockCatalog) {
				if (filter == ItemType.NONE || item.getTipo() == filter) {
					Item copy = copyItem(item);
					if (copy != null) {
						result.add(copy);
					}
				}
			}
		}
		return result;
	}

	private void seedMockLoans() {
		if (!mockLoaned.isEmpty() || !mockPending.isEmpty()) {
			return;
		}

		LocalDate today = LocalDate.now();

		mockLoaned.add(new LoanInfo(nextLoanId(), 1, "Il Visconte Dimezzato", ItemType.LIBRO, "alice",
				today.minusDays(3), today.plusDays(11)));
		mockLoaned.add(new LoanInfo(nextLoanId(), 7, "Kind of Blue", ItemType.CD, "bruno",
				today.minusDays(5), today.plusDays(9)));

		mockPending.add(new PendingLoanInfo(nextRequestId(), 4, "Neuromancer", ItemType.LIBRO, "carla",
				today.minusDays(1)));
		mockPending.add(new PendingLoanInfo(nextRequestId(), 9, "National Geographic 2022/12", ItemType.RIVISTA,
				"elena", today.minusDays(2)));
		mockPending.add(new PendingLoanInfo(nextRequestId(), 2, "Random Access Memories", ItemType.CD, "dario",
				today.minusDays(4)));
	}

	private int nextLoanId() {
		return ++nextLoanId;
	}

	private int nextRequestId() {
		return ++nextRequestId;
	}

	private int nextCatalogItemId() {
		return ++nextCatalogId;
	}

	private static LoanInfo copyLoan(LoanInfo loan) {
		if (loan == null) {
			return null;
		}
		return new LoanInfo(
				loan.getId(),
				loan.getItemId(),
				loan.getItemName(),
				loan.getCategory(),
				loan.getBorrower(),
				loan.getRequestedOn(),
				loan.getDueDate());
	}

	private static PendingLoanInfo copyPending(PendingLoanInfo pending) {
		if (pending == null) {
			return null;
		}
		return new PendingLoanInfo(
				pending.getId(),
				pending.getItemId(),
				pending.getItemName(),
				pending.getCategory(),
				pending.getBorrower(),
				pending.getRequestedOn());
	}

	private static Item copyItem(Item item) {
		if (item == null) {
			return null;
		}
		if (item instanceof Libro libro) {
			Libro copy = new Libro();
			copy.setId(libro.getId());
			copy.setTitolo(libro.getTitolo());
			copy.setQuantita(libro.getQuantita());
			copy.setAutore(libro.getAutore());
			copy.setGenere(libro.getGenere());
			copy.setIsbn(libro.getIsbn());
			return copy;
		}
		if (item instanceof CD cd) {
			CD copy = new CD();
			copy.setId(cd.getId());
			copy.setTitolo(cd.getTitolo());
			copy.setQuantita(cd.getQuantita());
			copy.setArtista(cd.getArtista());
			copy.setGenere(cd.getGenere());
			return copy;
		}
		if (item instanceof Rivista rivista) {
			Rivista copy = new Rivista();
			copy.setId(rivista.getId());
			copy.setTitolo(rivista.getTitolo());
			copy.setQuantita(rivista.getQuantita());
			copy.setAnno(rivista.getAnno());
			copy.setNumero(rivista.getNumero());
			return copy;
		}
		Item copy = new Item();
		copy.setId(item.getId());
		copy.setTitolo(item.getTitolo());
		copy.setQuantita(item.getQuantita());
		copy.setTipo(item.getTipo());
		return copy;
	}

	private void ensureItemType(Item item) {
		if (item == null) {
			return;
		}
		if (item.getTipo() != null && item.getTipo() != ItemType.NONE) {
			return;
		}
		if (item instanceof Libro) {
			item.setTipo(ItemType.LIBRO);
		} else if (item instanceof CD) {
			item.setTipo(ItemType.CD);
		} else if (item instanceof Rivista) {
			item.setTipo(ItemType.RIVISTA);
		} else {
			item.setTipo(ItemType.NONE);
		}
	}

	private void logMockAction(String prefix, String username, Item item) {
		try {
			System.out.println("[MOCK] " + prefix + " item richiesto da " + username + ": " + Converter.objectToJson(item));
		} catch (JacksonException e) {
			System.out.println("[MOCK] " + prefix + " item richiesto da " + username + ": " + item);
		}
	}

	private void ensureUserSamples(String username) {
		if (username == null || username.isBlank()) {
			return;
		}

		boolean hasLoan = mockLoaned.stream().anyMatch(loan -> username.equalsIgnoreCase(loan.getBorrower()));
		boolean hasPending = mockPending.stream().anyMatch(req -> username.equalsIgnoreCase(req.getBorrower()));

		if (hasLoan && hasPending) {
			return;
		}

		LocalDate today = LocalDate.now();

		if (!hasLoan) {
			mockLoaned.add(new LoanInfo(nextLoanId(), 101, "Manuale di Java", ItemType.LIBRO, username,
					today.minusDays(2), today.plusDays(12)));
			mockLoaned.add(new LoanInfo(nextLoanId(), 305, "Lezioni di Rock", ItemType.CD, username,
					today.minusDays(6), today.plusDays(6)));
		}

		if (!hasPending) {
			mockPending.add(new PendingLoanInfo(nextRequestId(), 808, "Rivista Scienza 2024/01", ItemType.RIVISTA,
					username, today.minusDays(1)));
		}
	}
}
