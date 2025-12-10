package zekusan.models.users;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import zekusan.app.systems.DataFiles;
import zekusan.net.Converter;


public class UsersList {
	private static UsersList getInstance() {
		return instance;
	}

	public static List<User> getUsers() {
		return UsersList.getInstance().getList();
	}

	private UsersList () {
		try {
			updateList();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void updateList() throws IOException {
		String fileName = USER_LIST_FILENAME;

		Path path = DataFiles.getDataFile(fileName);
		list.clear();

		try (BufferedReader buffer = Files.newBufferedReader(path)) {
			String line;

			while ((line = buffer.readLine()) != null) {
				User newUser = Converter.jsonToUser(line);
				list.add(newUser);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private ArrayList<User> getList() {
		return list;
	}

	private static final UsersList instance = new UsersList();
	private ArrayList<User> list = new ArrayList<>();
	private static final String USER_LIST_FILENAME = "Users.jsonl";
}
