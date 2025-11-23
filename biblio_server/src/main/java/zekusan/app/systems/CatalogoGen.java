package zekusan.app.systems;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import zekusan.models.ItemType;
import zekusan.models.items.Item;

public class CatalogoGen {

	public CatalogoGen() {
		lista = new ArrayList<>();
	}

	public ArrayList<Item> getLista(ItemType type) throws Exception {
		if (lista.isEmpty()) {
			create(type);
		}

		return lista;
	}

	public void create(ItemType type) throws Exception {
		String filepath = getFilePath(type);

		if (filepath == null) {
			throw new Exception("Invalid Item type");
		}

		if (!lista.isEmpty()) {
			lista.removeAll(lista);
		}

		try (BufferedReader buffer = new BufferedReader(new FileReader(filepath))) {
			String line;

			while ((line = buffer.readLine()) != null) {
				Item newItem = Converter.jsonToItem(line, type);
				lista.add(newItem);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getFilePath(ItemType type) {

		// TODO: FILEPATHS MUST BE UPDATED!!! DONT USE THIS VALUES
		switch (type) {
		case LIBRO:
			return "libri.jsonl";
		case RIVISTA:
			return "riviste.jsonl";
		case CD:
			return "cd.jsonl";
		default:
			return null;
		}
	}

	private ArrayList<Item> lista;
}
