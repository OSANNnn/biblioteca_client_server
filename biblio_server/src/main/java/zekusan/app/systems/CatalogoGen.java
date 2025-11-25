package zekusan.app.systems;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import zekusan.models.ItemType;
import zekusan.models.items.Item;

public class CatalogoGen {

	public CatalogoGen() {
		lista = new ArrayList<>();
	}

	public ArrayList<Item> getLista(ItemType type) throws Exception {
		clear();
		create(type);

		return lista;
	}

	private void create(ItemType type) throws Exception {
		String filepath = getFilePath(type);

		if (filepath == null) {
			throw new Exception("Invalid Item type");
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
	
	//this method deletes the old file!
	public void updateCatalog(ArrayList<Item> newList, ItemType type) throws Exception {
		String filepath = getFilePath(type);
		lista = newList;

		if (filepath == null) {
			throw new Exception("Invalid Item type");
		}
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
			for (Item item : lista) {
				String json = Converter.objectToJson(item);
				writer.write(json);
				writer.newLine();
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void clear() {
		if (!lista.isEmpty()) {
			lista.clear();
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
