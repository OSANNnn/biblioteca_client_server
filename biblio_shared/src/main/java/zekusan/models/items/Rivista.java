package zekusan.models.items;

import zekusan.enums.ItemType;

public class Rivista extends Item {
	private int anno;
	private int numero;

	public Rivista() {
		this.tipo = ItemType.RIVISTA;
	}

	public int getAnno() {
		return anno;
	}

	public void setAnno(int anno) {
		this.anno = anno;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}
}
