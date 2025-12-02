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
		return create(type);
	}

	private static List<Item> create(ItemType type) throws IOException {
		String filepath = getFilePath(type);
		List<Item> newCatalogo = new ArrayList<>();

		if (filepath == null) {
			throw new IOException("Invalid Item type");
		}

		InputStreamReader in = new InputStreamReader(CatalogoGen.class.getResourceAsStream(filepath));
		try (BufferedReader buffer = new BufferedReader(in)) {
			String line;

			while ((line = buffer.readLine()) != null) {
				Item newItem = Converter.jsonToItem(line, type);
				newCatalogo.add(newItem);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return newCatalogo;
	}
	
	//this method deletes the old file!
	public static void updateCatalog(List<Item> catalogo, ItemType type) throws IOException {
		String filepath = getFilePath(type);

		if (filepath == null) {
			throw new IOException("Invalid Item type");
		}
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
			for (Item item : catalogo) {
				String json = Converter.objectToJson(item);
				writer.write(json);
				writer.newLine();
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
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
}
