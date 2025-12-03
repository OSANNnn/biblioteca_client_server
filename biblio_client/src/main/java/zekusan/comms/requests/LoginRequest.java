package zekusan.comms.requests;

import zekusan.enums.ActionType;

public class LoginRequest extends Request {
	private String password;

	public LoginRequest() {
		this.action = ActionType.LOGIN;
	}

	public LoginRequest(String username, String password) {
		this.action = ActionType.LOGIN;
		this.username = username;
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
