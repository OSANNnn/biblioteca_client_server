package zekusan.app.systems;

import zekusan.models.comms.Status;
import zekusan.models.comms.requests.LoginRequest;
import zekusan.models.comms.responses.LoginResponse;
import zekusan.models.users.User;
import zekusan.models.users.UsersList;

public class LoginSystem {

	private LoginSystem() {
		
	}
	
	private static boolean CheckCredentials(LoginRequest request) {
		boolean status = false;
		
		for(User u : UsersList.getUsers())
		{
			if (u.getUsername().equals(request.getUsername()) &&
					u.getPassword().equals(request.getPassword()))
				status = true;
		}
		
		return status;
	}
	
	public static LoginResponse Login(LoginRequest req) {
		LoginResponse resp = new LoginResponse();
		
		if (CheckCredentials(req)) {
			resp.setStatus(Status.SUCCESS);
			resp.setToken(TokenSystem.generateToken());
		}
		else {
			resp.setStatus(Status.FAILED);
			resp.setToken(-1);
		}
		
		return resp;
	}

	//private static final LoginSystem instance = new LoginSystem();
}
