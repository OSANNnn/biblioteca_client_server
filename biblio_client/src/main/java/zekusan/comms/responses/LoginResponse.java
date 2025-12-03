package zekusan.comms.responses;

import zekusan.enums.ActionType;
import zekusan.enums.UserType;

public class LoginResponse extends Response {
	private int token;
	private UserType userType;

	public LoginResponse() {
		this.action = ActionType.LOGIN;
	}

	public LoginResponse(int token) {
		this.action = ActionType.LOGIN;
		this.token = token;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}
}
