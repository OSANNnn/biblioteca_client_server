package zekusan.models.items;

import com.fasterxml.jackson.core.JacksonException;

import zekusan.enums.ItemType;
import zekusan.net.Converter;

public class CD extends Item {
	private String artista;
	private String genere;

	public CD() {
		this.tipo = ItemType.CD;
	}

	public String toJson() throws JacksonException {
		return Converter.objectToJson(this);
	}

	public String getArtista() {
		return artista;
	}

	public void setArtista(String artista) {
		this.artista = artista;
	}

	public String getGenere() {
		return genere;
	}

	public void setGenere(String genere) {
		this.genere = genere;
	}
}
