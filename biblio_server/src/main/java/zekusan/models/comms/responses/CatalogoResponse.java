package zekusan.models.comms.responses;

import java.util.List;

import zekusan.models.ItemType;
import zekusan.models.comms.ActionType;
import zekusan.models.items.Item;

public class CatalogoResponse extends Response {
	
	public CatalogoResponse () {
		action = ActionType.CATALOGO;
	}
	
	public CatalogoResponse (List<Item> catalogo, ItemType categoria) {
		action = ActionType.CATALOGO;
		this.catalogo = catalogo;
		this.categoria = categoria;
	}

	public List<Item> getCatalogo() {
		return catalogo;
	}

	public void setCatalogo(List<Item> catalogo) {
		this.catalogo = catalogo;
	}

	public ItemType getCategoria() {
		return categoria;
	}

	public void setCategoria(ItemType categoria) {
		this.categoria = categoria;
	}

	private List<Item> catalogo;
	private ItemType categoria;
}
