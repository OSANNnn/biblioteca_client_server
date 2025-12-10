package zekusan.app.systems;

import zekusan.comms.requests.LoginRequest;
import zekusan.comms.responses.LoginResponse;
import zekusan.enums.Status;
import zekusan.models.users.User;
import zekusan.models.users.UsersList;

public class LoginSystem {

	private static User findUser(LoginRequest request) {
		for (User u : UsersList.getUsers()) {
			if (u.getUsername().equals(request.getUsername()) && u.getPassword().equals(request.getPassword())) {
				return u;
			}
		}

		return null;
	}

	public static LoginResponse login(LoginRequest req) {
		LoginResponse resp = new LoginResponse();
		User user = findUser(req);

		if (user != null) {
			int generatedToken = TokenSystem.generateToken();
			resp.setStatus(Status.SUCCESS);
			resp.setToken(generatedToken);
			resp.setUserType(user.getUserType());
			SessionSystem.createSession(req.getUsername(), generatedToken);
		} else {
			resp.setStatus(Status.FAILED);
			resp.setToken(ERROR_TOKEN);
		}

		return resp;
	}

	private LoginSystem() {

	}

	static final int ERROR_TOKEN = -1;
}
