package zekusan.models.comms.requests;

import zekusan.models.ItemType;
import zekusan.models.comms.ActionType;

public class CatalogoRequest extends Request {
	public CatalogoRequest() {
		action = ActionType.CATALOGO;
	}
	
	public CatalogoRequest (int token, String username, ItemType categoria) {
		action = ActionType.CATALOGO;
		this.categoria = categoria;
		this.username = username;
		this.token = token;
	}
	
	public ItemType getCategoria() {
		return categoria;
	}

	public void setCategoria(ItemType categoria) {
		this.categoria = categoria;
	}

	ItemType categoria;
}
