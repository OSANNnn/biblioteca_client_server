package zekusan;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;

import zekusan.app.systems.*;
import zekusan.models.comms.requests.*;
import zekusan.models.comms.responses.*;

public class App {
	public static void main(String[] args) {
		SocketSystem socket = new SocketSystem();

		if (!socket.init()) {
			return;
		}

		while (socket.isOnline()) {
			try {
				socket.listen();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String jsonResponse = null; // null to be removed?
			Response response = null; // null to be removed?
			String jsonRequest = socket.getRequest();
			Request request = RequestHandler.process(jsonRequest);
			switch (request.getAction()) {
			case LOGIN: {
				response = LoginSystem.Login((LoginRequest) request);
				jsonResponse = "login|";
				break;
			}
			case CATALOGO: {
				if (SessionSystem.validateSession(request.getUsername(), request.getToken())) {

				} else {

				}
				jsonResponse = "catalogo|";
				break;
			}
			case PRENOTAZIONE: {
				if (SessionSystem.validateSession(request.getUsername(), request.getToken())) {

				} else {

				}
				jsonResponse = "prenotazione|";
				break;
			}
			case NONE: {
				jsonResponse = "NONE|";
				break;
			}
			default:
				break;
			}

			try {
				jsonResponse += Converter.objectToJson(response);
			} catch (JacksonException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				System.out.println(e);
			}

			socket.sendResponse(jsonResponse);
			socket.closeConnection();
		}

	}
}
