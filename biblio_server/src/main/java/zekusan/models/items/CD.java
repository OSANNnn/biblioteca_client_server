package zekusan.models.items;

import com.fasterxml.jackson.core.JacksonException;

import zekusan.app.systems.Converter;

public class CD extends Item {
	public String toString() {

		String json = "";

		try {
			json = Converter.objectToJson(this);
		} catch (JacksonException e) {
			System.out.println("ERROR (JsonConverter): " + e.getMessage() + " for CD id: " + id);
		}

		return json;
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
