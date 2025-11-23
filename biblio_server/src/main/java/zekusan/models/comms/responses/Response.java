package zekusan.models.comms.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;

import zekusan.models.comms.ActionType;
import zekusan.models.comms.Status;

public class Response {
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public ActionType getAction() {
		return action;
	}

	public void setAction(ActionType action) {
		this.action = action;
	}

	@JsonIgnore
	protected ActionType action;
	protected Status status;
}
