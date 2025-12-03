package zekusan.net;

import java.io.IOException;
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

public class ApiClient {
	private final SocketClient socketClient;

	public ApiClient(SocketClient socketClient) {
		this.socketClient = socketClient;
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
}
