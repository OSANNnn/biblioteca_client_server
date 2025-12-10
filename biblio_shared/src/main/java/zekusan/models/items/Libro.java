package zekusan.models.items;

import com.fasterxml.jackson.core.JacksonException;

import zekusan.enums.ItemType;
import zekusan.net.Converter;

public class Libro extends Item {
	private String genere;
	private String autore;
	private String isbn;

	public Libro() {
		this.tipo = ItemType.LIBRO;
	}

	public String toJson() throws JacksonException {
		return Converter.objectToJson(this);
	}

	public String getGenere() {
		return genere;
	}

	public void setGenere(String genere) {
		this.genere = genere;
	}

	public String getAutore() {
		return autore;
	}

	public void setAutore(String autore) {
		this.autore = autore;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
}
