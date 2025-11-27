package zekusan.models.comms.responses;

import zekusan.models.ItemType;
import zekusan.models.comms.ActionType;

public class PrenotazioneResponse extends Response {
	public PrenotazioneResponse () {
		action = ActionType.PRENOTAZIONE;
	}
	
	public PrenotazioneResponse (int id, ItemType type) {
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

	int itemId;
	ItemType type;
}
