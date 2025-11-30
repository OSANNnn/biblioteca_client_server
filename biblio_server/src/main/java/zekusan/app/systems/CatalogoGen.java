package zekusan.app.systems;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import zekusan.models.ItemType;
import zekusan.models.items.Item;

public class CatalogoGen {

	private CatalogoGen() {
	}

	public static List<Item> getLista(ItemType type) throws IOException {
		clear();
		create(type);

		return lista;
	}

	private static void create(ItemType type) throws IOException {
		String filepath = getFilePath(type);

		if (filepath == null) {
			throw new IOException("Invalid Item type");
		}

		InputStreamReader in = new InputStreamReader(CatalogoGen.class.getResourceAsStream(filepath));
		try (BufferedReader buffer = new BufferedReader(in)) {
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
	public static void updateCatalog(List<Item> newList, ItemType type) throws IOException {
		String filepath = getFilePath(type);
		lista = newList;

		if (filepath == null) {
			throw new IOException("Invalid Item type");
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

	public static void clear() {
		if (!lista.isEmpty()) {
			lista.clear();
		}
	}

	private static String getFilePath(ItemType type) {

		switch (type) {
		case LIBRO:
			return "/Libri.jsonl";
		case RIVISTA:
			return "/Riviste.jsonl";
		case CD:
			return "/Cd.jsonl";
		default:
			return null;
		}
	}

	private static List<Item> lista = new ArrayList<>();
}
