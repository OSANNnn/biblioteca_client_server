package zekusan.app.systems;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import zekusan.enums.ItemType;
import zekusan.models.items.Item;
import zekusan.net.Converter;

public class CatalogoGen {

	private CatalogoGen() {
	}

	public static List<Item> getLista(ItemType type) throws IOException {
		return create(type);
	}

	
	private static List<Item> create(ItemType type) throws IOException {
		List<Item> newCatalogo = new ArrayList<>();
		String fileName = getFileName(type);
		
		if (fileName == null) {
			throw new IOException("Invalid Item type");
		}
		
		Path path = DataFiles.getDataFile(fileName);

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			String line;

			while ((line = reader.readLine()) != null) {
				Item newItem = Converter.jsonToItem(line, type);
				newCatalogo.add(newItem);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return newCatalogo;
	}
	
	//this method deletes the old file!
	public static void updateCatalog(List<Item> catalogo, ItemType type) throws IOException {
		String fileName = getFileName(type);
		
		if (fileName == null) {
			throw new IOException("Invalid Item type");
		}
		
		Path path = DataFiles.getDataFile(fileName);

		try (BufferedWriter writer = Files.newBufferedWriter(path, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.TRUNCATE_EXISTING)) {
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

	private static String getFileName(ItemType type) {

		switch (type) {
		case LIBRO:
			return "Libri.jsonl";
		case RIVISTA:
			return "Riviste.jsonl";
		case CD:
			return "Cd.jsonl";
		default:
			return null;
		}
	}
}
