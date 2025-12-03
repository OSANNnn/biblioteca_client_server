package zekusan.models.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import zekusan.enums.ItemType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
	protected int id;
	protected String titolo;
	protected int quantita;
	protected ItemType tipo;

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
}
