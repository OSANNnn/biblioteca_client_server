package zekusan.services;

import java.io.IOException;
import java.util.List;

import zekusan.enums.ItemType;
import zekusan.enums.Status;
import zekusan.enums.UserType;
import zekusan.comms.responses.CatalogoResponse;
import zekusan.comms.responses.LoginResponse;
import zekusan.comms.responses.PrenotazioneResponse;
import zekusan.models.loans.LoanInfo;
import zekusan.models.loans.PendingLoanInfo;
import zekusan.models.items.Item;
import zekusan.net.ApiClient;

public class LibraryClient {
	private final ApiClient apiClient;
	private Session session;
	private transient Item pendingEditItem;

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

	public List<LoanInfo> loadLoanedItems() throws IOException {
		ensureLoggedIn();
		return apiClient.fetchLoanedItems(session.token(), session.username());
	}

	public List<PendingLoanInfo> loadPendingLoans() throws IOException {
		ensureLoggedIn();
		return apiClient.fetchPendingLoans(session.token(), session.username());
	}

	public List<LoanInfo> loadMyBorrowedItems() throws IOException {
		ensureLoggedIn();
		return apiClient.fetchUserLoanedItems(session.token(), session.username());
	}

	public List<PendingLoanInfo> loadMyPendingBorrows() throws IOException {
		ensureLoggedIn();
		return apiClient.fetchUserPendingLoans(session.token(), session.username());
	}

	public LoanInfo acceptPendingLoan(int requestId) throws IOException {
		ensureLoggedIn();
		return apiClient.acceptPendingLoan(session.token(), session.username(), requestId);
	}

	public boolean cancelPendingLoan(int requestId) throws IOException {
		ensureLoggedIn();
		return apiClient.cancelPendingLoan(session.token(), session.username(), requestId);
	}

	public boolean returnLoan(int loanId) throws IOException {
		ensureLoggedIn();
		return apiClient.returnLoan(session.token(), session.username(), loanId);
	}

	public boolean cancelBorrowRequest(int requestId) throws IOException {
		ensureLoggedIn();
		return apiClient.cancelPendingBorrow(session.token(), session.username(), requestId);
	}

	public Item addItem(Item item) throws IOException {
		ensureLoggedIn();
		return apiClient.addItem(session.token(), session.username(), item);
	}

	public Item updateItem(Item item) throws IOException {
		ensureLoggedIn();
		return apiClient.updateItem(session.token(), session.username(), item);
	}

	public Item saveItem(Item item) throws IOException {
		if (item == null) {
			throw new IllegalArgumentException("Item is required");
		}
		ensureLoggedIn();
		return item.getId() > 0 ? apiClient.updateItem(session.token(), session.username(), item)
				: apiClient.addItem(session.token(), session.username(), item);
	}

	public boolean deleteItem(int itemId) throws IOException {
		ensureLoggedIn();
		return apiClient.deleteItem(session.token(), session.username(), itemId);
	}

	public void setPendingEditItem(Item item) {
		this.pendingEditItem = item;
	}

	public Item consumePendingEditItem() {
		Item item = this.pendingEditItem;
		this.pendingEditItem = null;
		return item;
	}

	private void ensureLoggedIn() {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Session is not valid");
		}
	}
}
