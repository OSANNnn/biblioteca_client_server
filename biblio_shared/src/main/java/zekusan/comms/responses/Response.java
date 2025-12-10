package zekusan.comms.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;

import zekusan.enums.ActionType;
import zekusan.enums.Status;

public class Response {
	@JsonIgnore
	protected ActionType action;
	protected Status status;

	public ActionType getAction() {
		return action;
	}

	public void setAction(ActionType action) {
		this.action = action;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
