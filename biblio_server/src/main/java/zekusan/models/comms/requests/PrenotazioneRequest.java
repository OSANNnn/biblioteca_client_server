package zekusan.models.comms.requests;

import zekusan.models.ItemType;
import zekusan.models.comms.ActionType;

public class PrenotazioneRequest extends Request {
	
	public PrenotazioneRequest() {
		action = ActionType.PRENOTAZIONE;
	}
	
	public PrenotazioneRequest (int id, ItemType type) {
		action = ActionType.PRENOTAZIONE;
		itemId = id;
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

	private int itemId;
	private ItemType type;
}
