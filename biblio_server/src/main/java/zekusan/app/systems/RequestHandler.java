package zekusan.app.systems;

import java.util.Map;

import com.fasterxml.jackson.core.JacksonException;

import zekusan.models.comms.ActionType;
import zekusan.models.comms.requests.Request;

public class RequestHandler {
	public static Request process(String request) {
		int separatorPosition = request.indexOf(SEPARATOR);
		String header = request.substring(0, separatorPosition);
		String body = request.substring(separatorPosition + 1);
		ActionType type = requestMap.get(header);
		Request obj = null;
		
		try {
			obj = Converter.jsonToRequest(body, type);
		} catch (JacksonException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
	private RequestHandler() {
		
	}
	
	private static Map<String, ActionType> requestMap = Map.of(
			"login", ActionType.LOGIN,
			"catalogo", ActionType.CATALOGO,
			"prenotazione", ActionType.PRENOTAZIONE);
	
	private static final char SEPARATOR = '|';
}
