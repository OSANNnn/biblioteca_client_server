package zekusan.net;

import java.util.Map;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import zekusan.enums.ActionType;
import zekusan.enums.ItemType;
import zekusan.enums.UserType;
import zekusan.comms.requests.CatalogoRequest;
import zekusan.comms.requests.LoginRequest;
import zekusan.comms.requests.PrenotazioneRequest;
import zekusan.comms.requests.Request;
import zekusan.comms.responses.CatalogoResponse;
import zekusan.comms.responses.LoginResponse;
import zekusan.comms.responses.PrenotazioneResponse;
import zekusan.comms.responses.Response;
import zekusan.models.items.CD;
import zekusan.models.items.Item;
import zekusan.models.items.Libro;
import zekusan.models.items.Rivista;
import zekusan.models.users.Studente;
import zekusan.models.users.User;

public final class Converter {
	private Converter() {
	}

	public static String objectToJson(Object object) throws JacksonException {
		if (object == null) {
			throw new IllegalArgumentException("Object is null");
		}

		return mapper.writeValueAsString(object);
	}

	public static Request jsonToRequest(String jsonString, ActionType type) throws JacksonException {
		Class<? extends Request> classType = requestMap.get(type);

		if (classType == null) {
			throw new IllegalArgumentException("Request type not supported");
		}

		return mapper.readValue(jsonString, classType);
	}

	public static Response jsonToResponse(String jsonString, ActionType type) throws JacksonException {
		Class<? extends Response> classType = responseMap.get(type);

		if (classType == null) {
			throw new IllegalArgumentException("Response type not supported");
		}

		return mapper.readValue(jsonString, classType);
	}

	public static Item jsonToItem(String jsonString, ItemType type) throws JacksonException {
		Class<? extends Item> classType = itemMap.get(type);

		if (classType == null) {
			throw new IllegalArgumentException("Item type not supported");
		}

		return mapper.readValue(jsonString, classType);
	}

	public static ObjectMapper mapper() {
		return mapper;
	}

	public static User jsonToUser(String jsonString) throws JacksonException {
		Class<? extends User> classType = userMap.get(UserType.STUDENTE);
		return mapper.readValue(jsonString, classType);
	}

	private static final ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private static final Map<ActionType, Class<? extends Request>> requestMap = Map.of(
			ActionType.LOGIN, LoginRequest.class,
			ActionType.CATALOGO, CatalogoRequest.class,
			ActionType.PRENOTAZIONE, PrenotazioneRequest.class);

	private static final Map<ActionType, Class<? extends Response>> responseMap = Map.of(
			ActionType.LOGIN, LoginResponse.class,
			ActionType.CATALOGO, CatalogoResponse.class,
			ActionType.PRENOTAZIONE, PrenotazioneResponse.class);

	private static final Map<ItemType, Class<? extends Item>> itemMap = Map.of(
			ItemType.LIBRO, Libro.class,
			ItemType.CD, CD.class,
			ItemType.RIVISTA, Rivista.class);

	private static final Map<UserType, Class<? extends User>> userMap = Map.of(
			UserType.STUDENTE, Studente.class);
}
