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
import zekusan.models.loans.LoanInfo;
import zekusan.models.loans.PendingLoanInfo;
import zekusan.models.items.Item;

public class ApiClient {
	private final SocketClient socketClient;
	private final List<LoanInfo> mockLoaned = new ArrayList<>();
	private final List<PendingLoanInfo> mockPending = new ArrayList<>();
	private int nextLoanId = 1000;
	private int nextRequestId = 2000;

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
		return sendCatalogo(request);
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
}
