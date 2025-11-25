package zekusan.models.comms.requests;

import zekusan.models.ItemType;

public class PrenotazioneRequest extends Request {

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
