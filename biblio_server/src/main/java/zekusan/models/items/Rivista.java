package zekusan.models.items;

import com.fasterxml.jackson.core.JacksonException;

import zekusan.app.systems.Converter;

public class Rivista extends Item {
	
	public String toString() {

		String json = "";

		try {
			json = Converter.objectToJson(this);
		} catch (JacksonException e) {
			System.out.println("ERROR (JsonConverter): " + e.getMessage() + " for Rivista id: " + id);
		}

		return json;
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

	private int anno;
	private int numero;
}
