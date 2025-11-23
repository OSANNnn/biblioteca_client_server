package zekusan.models.comms.requests;

import zekusan.models.ItemType;

public class CatalogoRequest extends Request {
	public CatalogoRequest() {
	}
	
	public ItemType getCategoria() {
		return categoria;
	}

	public void setCategoria(ItemType categoria) {
		this.categoria = categoria;
	}

	ItemType categoria;
}
