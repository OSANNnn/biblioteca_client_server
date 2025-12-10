package zekusan.comms.requests;

import zekusan.enums.ActionType;
import zekusan.enums.ItemType;

public class PrenotazioneRequest extends Request {

	private int itemId;
	private ItemType type;

	public PrenotazioneRequest() {
		action = ActionType.PRENOTAZIONE;
	}

	public PrenotazioneRequest(int id, ItemType type) {
		action = ActionType.PRENOTAZIONE;
		this.itemId = id;
		this.type = type;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}
}
