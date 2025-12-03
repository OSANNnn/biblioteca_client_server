package zekusan.comms.responses;

import java.util.List;

import zekusan.enums.ActionType;
import zekusan.enums.ItemType;
import zekusan.models.items.Item;

public class CatalogoResponse extends Response {
	private List<Item> catalogo;
	private ItemType categoria;

	public CatalogoResponse() {
		this.action = ActionType.CATALOGO;
	}

	public CatalogoResponse(List<Item> catalogo, ItemType categoria) {
		this.action = ActionType.CATALOGO;
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
}
