package zekusan.models.comms.responses;

import zekusan.models.comms.ActionType;

public class LoginResponse extends Response {

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}
	
	public LoginResponse() {
		action = ActionType.LOGIN;
	}
	
	private int token;
}
