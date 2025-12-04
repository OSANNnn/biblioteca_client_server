package zekusan.models.users;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import zekusan.app.systems.Converter;


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
		
		ClassLoader cl = UsersList.class.getClassLoader();
		URL url = cl.getResource("data/" + fileName);
		Path path = null;
		
		try {
			path = Paths.get(url.toURI());
		} catch (URISyntaxException e) {
			System.out.println(e);
		}
		
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
