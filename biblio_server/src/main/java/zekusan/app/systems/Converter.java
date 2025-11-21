package zekusan.app.systems;

import java.util.Map;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;

import zekusan.models.ItemType;
import zekusan.models.comms.ActionType;
import zekusan.models.comms.requests.*;
import zekusan.models.comms.responses.*;
import zekusan.models.items.*;

public class Converter {
	static public String objectToJson(Object object) throws JacksonException {
		String json = "";

		json = mapper.writeValueAsString(object);

		return json;
	}

	static public Request jsonToRequest(String jsonString, ActionType type) throws JacksonException {
		Class<? extends Request> classType = requestMap.get(type);

		if (classType == null)
			throw new IllegalArgumentException("Request type not supported");

		return mapper.readValue(jsonString, classType);
	}

	static public Response jsonToResponse(String jsonString, ActionType type) throws JacksonException {

		Class<? extends Response> classType = responseMap.get(type);

		if (classType == null)
			throw new IllegalArgumentException("Response type not supported");

		return mapper.readValue(jsonString, classType);

	}

	static public Item jsonToItem(String jsonString, ItemType type) throws JacksonException {

		Class<? extends Item> classType = itemMap.get(type);

		if (classType == null)
			throw new IllegalArgumentException("Item type not supported");

		return mapper.readValue(jsonString, classType);
	}

	private static final ObjectMapper mapper = new ObjectMapper();

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
}
