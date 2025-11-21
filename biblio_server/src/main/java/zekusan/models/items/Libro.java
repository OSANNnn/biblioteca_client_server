package zekusan.models.items;

import com.fasterxml.jackson.core.JacksonException;

import zekusan.app.systems.Converter;
import zekusan.models.ItemType;

public class Libro extends Item {

	public Libro() {
		tipo = ItemType.LIBRO;
	}

	public String toString() {

		String json = "";

		try {
			json = Converter.objectToJson(this);
		} catch (JacksonException e) {
			System.out.println("ERROR (JsonConverter): " + e.getMessage() + " for Libro id: " + id);
		}

		return json;
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

	private String genere;
	private String autore;
	private String isbn;
}
