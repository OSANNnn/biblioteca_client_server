package zekusan.models.comms.responses;

import java.util.ArrayList;

import zekusan.models.ItemType;
import zekusan.models.items.Item;

public class CatalogoResponse extends Response {

	public ArrayList<? extends Item> getCatalogo() {
		return catalogo;
	}

	public void setCatalogo(ArrayList<? extends Item> catalogo) {
		this.catalogo = catalogo;
	}

	public ItemType getCategoria() {
		return categoria;
	}

	public void setCategoria(ItemType categoria) {
		this.categoria = categoria;
	}

	ArrayList<? extends Item> catalogo;
	ItemType categoria;
}
