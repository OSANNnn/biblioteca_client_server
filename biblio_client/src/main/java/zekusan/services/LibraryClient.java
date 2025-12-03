package zekusan.services;

import java.io.IOException;

import zekusan.enums.ItemType;
import zekusan.enums.Status;
import zekusan.enums.UserType;
import zekusan.comms.responses.CatalogoResponse;
import zekusan.comms.responses.LoginResponse;
import zekusan.comms.responses.PrenotazioneResponse;
import zekusan.net.ApiClient;

public class LibraryClient {
	private final ApiClient apiClient;
	private Session session;

	public LibraryClient(ApiClient apiClient) {
		this.apiClient = apiClient;
	}

	public LoginResponse login(String username, String password) throws IOException {
		LoginResponse response = apiClient.login(username, password);
		if (response.getStatus() == Status.SUCCESS && response.getToken() > 0) {
			UserType role = response.getUserType() == null ? UserType.STUDENTE : response.getUserType();
			session = new Session(username, response.getToken(), role);
		} else {
			session = null;
		}
		return response;
	}

	public void logout() {
		session = null;
	}

	public boolean isLoggedIn() {
		return session != null && session.isValid();
	}

	public Session getSession() {
		return session;
	}

	public CatalogoResponse loadCatalog(ItemType category) throws IOException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Session is not valid");
		}
		return apiClient.fetchCatalogo(session.token(), session.username(), category);
	}

	public PrenotazioneResponse prenota(int itemId, ItemType type) throws IOException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Session is not valid");
		}
		return apiClient.prenota(session.token(), session.username(), itemId, type);
	}
}
