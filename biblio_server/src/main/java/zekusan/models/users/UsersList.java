package zekusan.models.users;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
		String filepath = USER_LIST_PATH;
		
		InputStreamReader in = new InputStreamReader(UsersList.class.getResourceAsStream(filepath));
		
		try (BufferedReader buffer = new BufferedReader(in)) {
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
	private static final String USER_LIST_PATH = "/Users.jsonl";
}
