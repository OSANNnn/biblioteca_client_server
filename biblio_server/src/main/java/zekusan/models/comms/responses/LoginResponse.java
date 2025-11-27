package zekusan.models.comms.responses;

import zekusan.models.comms.ActionType;

public class LoginResponse extends Response {
	
	public LoginResponse() {
		action = ActionType.LOGIN;
	}
	
	public LoginResponse(int token) {
		action = ActionType.LOGIN;
		this.token = token;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}
	
	private int token;
}
