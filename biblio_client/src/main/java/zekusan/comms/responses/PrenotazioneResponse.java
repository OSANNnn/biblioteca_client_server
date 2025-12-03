package zekusan.comms.responses;

import zekusan.enums.ActionType;
import zekusan.enums.ItemType;

public class PrenotazioneResponse extends Response {
	int itemId;
	ItemType type;

	public PrenotazioneResponse() {
		action = ActionType.PRENOTAZIONE;
	}

	public PrenotazioneResponse(int id, ItemType type) {
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
