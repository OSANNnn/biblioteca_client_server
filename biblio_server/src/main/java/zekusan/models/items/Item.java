package zekusan.models.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import zekusan.models.ItemType;

public class Item {
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitolo() {
		return titolo;
	}

	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}

	public int getQuantita() {
		return quantita;
	}

	public void setQuantita(int quantita) {
		this.quantita = quantita;
	}

	public ItemType getTipo() {
		return tipo;
	}

	public void setTipo(ItemType tipo) {
		this.tipo = tipo;
	}

	private int id;
	private String titolo;
	private int quantita;
	@JsonIgnoreProperties
	ItemType tipo;
}
