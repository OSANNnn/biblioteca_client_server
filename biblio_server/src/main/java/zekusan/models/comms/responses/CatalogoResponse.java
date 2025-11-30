package zekusan.models.comms.responses;

import java.util.ArrayList;

import zekusan.models.ItemType;
import zekusan.models.comms.ActionType;
import zekusan.models.items.Item;

public class CatalogoResponse extends Response {
	
	public CatalogoResponse () {
		action = ActionType.CATALOGO;
	}
	
	public CatalogoResponse (ArrayList<Item> catalogo, ItemType categoria) {
		action = ActionType.CATALOGO;
		this.catalogo = catalogo;
		this.categoria = categoria;
	}

	public ArrayList<Item> getCatalogo() {
		return catalogo;
	}

	public void setCatalogo(ArrayList<Item> catalogo) {
		this.catalogo = catalogo;
	}

	public ItemType getCategoria() {
		return categoria;
	}

	public void setCategoria(ItemType categoria) {
		this.categoria = categoria;
	}

	private ArrayList<Item> catalogo;
	private ItemType categoria;
}
