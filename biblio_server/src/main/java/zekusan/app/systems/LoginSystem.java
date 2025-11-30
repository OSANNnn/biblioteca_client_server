package zekusan.app.systems;

import zekusan.models.comms.Status;
import zekusan.models.comms.requests.LoginRequest;
import zekusan.models.comms.responses.LoginResponse;
import zekusan.models.users.User;
import zekusan.models.users.UsersList;

public class LoginSystem {
	
	private static boolean checkCredentials(LoginRequest request) {
		boolean status = false;
		
		for(User u : UsersList.getUsers())
		{
			if (u.getUsername().equals(request.getUsername()) &&
					u.getPassword().equals(request.getPassword()))
				status = true;
		}
		
		return status;
	}
	
	public static LoginResponse login(LoginRequest req) {
		LoginResponse resp = new LoginResponse();
		
		if (checkCredentials(req)) {
			resp.setStatus(Status.SUCCESS);
			resp.setToken(TokenSystem.generateToken());
			SessionSystem.createSession(req.getUsername(), req.getToken());
		}
		else {
			resp.setStatus(Status.FAILED);
			resp.setToken(ERROR_TOKEN);
		}
		
		return resp;		
	}
	
	private LoginSystem() {
		
	}
	
	static final int ERROR_TOKEN = -1;
}
