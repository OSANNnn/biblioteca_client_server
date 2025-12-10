package zekusan.comms.requests;

import zekusan.enums.ActionType;
import zekusan.enums.ItemType;

public class CatalogoRequest extends Request {
	private ItemType categoria;

	public CatalogoRequest() {
		this.action = ActionType.CATALOGO;
	}

	public CatalogoRequest(int token, String username, ItemType categoria) {
		this.action = ActionType.CATALOGO;
		this.token = token;
		this.username = username;
		this.categoria = categoria;
	}

	public ItemType getCategoria() {
		return categoria;
	}

	public void setCategoria(ItemType categoria) {
		this.categoria = categoria;
	}
}
