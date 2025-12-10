package zekusan.comms.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;

import zekusan.enums.ActionType;

public class Request {
	@JsonIgnore
	protected ActionType action;
	protected int token;
	protected String username;

	public Request() {
		// default constructor for Jackson
	}

	public ActionType getAction() {
		return action;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
