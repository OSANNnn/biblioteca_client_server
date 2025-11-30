package zekusan.models.comms.requests;

import zekusan.models.comms.ActionType;

public class LoginRequest extends Request {
	public LoginRequest(String user, String pass) {
		username = user;
		password = pass;
		action = ActionType.LOGIN;
	}
	
	LoginRequest() {
		action = ActionType.LOGIN;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	String password;
}
