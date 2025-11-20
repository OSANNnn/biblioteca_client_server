package zekusan.models.comms.requests;

import zekusan.models.comms.ActionType;

abstract public class Request {
	public Request() {}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}

	public ActionType getAction() {
		return action;
	}

	public void setAction(ActionType action) {
		this.action = action;
	}

	private int token;
	private ActionType action;
}