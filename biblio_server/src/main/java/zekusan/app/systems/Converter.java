package zekusan.app.systems;

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
		ObjectMapper mapper = new ObjectMapper();

		json = mapper.writeValueAsString(object);

		return json;
	}

	static public Request jsonToRequest(String jsonString, ActionType type) throws JacksonException {
		Request instance;
		ObjectMapper mapper = new ObjectMapper();

		switch (type) {
		case LOGIN: {
			instance = mapper.readValue(jsonString, LoginRequest.class);
			break;
		}
		case CATALOGO: {
			instance = mapper.readValue(jsonString, CatalogoRequest.class);
			break;
		}
		case PRENOTAZIONE: {
			instance = mapper.readValue(jsonString, PrenotazioineRequest.class);
			break;
		}
		default: {
			instance = null;
			break;
		}
		} //switch(type)

		return instance;
	}

	static public Response jsonToResponse(String jsonString, ActionType type) throws JacksonException {
		Response instance;
		ObjectMapper mapper = new ObjectMapper();

		switch (type) {
		case LOGIN: {
			instance = mapper.readValue(jsonString, LoginResponse.class);
			break;
		}
		case CATALOGO: {
			instance = mapper.readValue(jsonString, CatalogoResponse.class);
			break;
		}
		case PRENOTAZIONE: {
			instance = mapper.readValue(jsonString, PrenotazioineResponse.class);
			break;
		}
		default: {
			instance = null;
			break;
		}
		} //switch(type)

		return instance;
	}

	static public Item jsonToItem(String jsonString, ItemType type) throws JacksonException {

		Item instance;
		ObjectMapper mapper = new ObjectMapper();

		switch (type) {
		case LIBRO: {
			instance = mapper.readValue(jsonString, Libro.class);
			break;
		}
		case CD: {
			instance = mapper.readValue(jsonString, CD.class);
			break;
		}
		case RIVISTA: {
			instance = mapper.readValue(jsonString, Rivista.class);
			break;
		}
		default:
			instance = null;
			break;

		} //switch(type)

		return instance;
	}
}
