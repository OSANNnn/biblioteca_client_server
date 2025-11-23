package zekusan.models.items;

import com.fasterxml.jackson.core.JacksonException;

import zekusan.app.systems.Converter;
import zekusan.models.ItemType;

public class CD extends Item {
	public CD() {
		tipo = ItemType.CD;
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

	private String artista;
	private String genere;
}
