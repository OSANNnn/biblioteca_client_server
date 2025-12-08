package zekusan.app.systems;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides writable data file locations seeded from classpath resources.
 */
public final class DataFiles {

	private static final String RESOURCE_ROOT = "data/";
	private static final Path DATA_DIR = Paths.get(System.getProperty("user.dir"), "data");

	private DataFiles() {
	}

	public static Path getDataFile(String fileName) throws IOException {
		Files.createDirectories(DATA_DIR);

		Path target = DATA_DIR.resolve(fileName);

		if (Files.notExists(target)) {
			copySeed(fileName, target);
		}

		return target;
	}

	private static void copySeed(String fileName, Path target) throws IOException {
		String resourcePath = RESOURCE_ROOT + fileName;

		try (InputStream stream = DataFiles.class.getClassLoader().getResourceAsStream(resourcePath)) {
			if (stream == null) {
				throw new IOException("Missing classpath resource: " + resourcePath);
			}
			Files.copy(stream, target);
		}
	}
}
