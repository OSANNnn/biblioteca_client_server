package zekusan.models.users;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import zekusan.app.systems.Converter;


public class UsersList {
	private static UsersList GetInstance() {
		return instance;
	}

	public static ArrayList<User> getUsers() {
		return UsersList.GetInstance().getList();
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
		String filepath = getFilePath();

		if (filepath == null) {
			throw new IOException("Invalid userslist file");
		}

		try (BufferedReader buffer = new BufferedReader(new FileReader(filepath))) {
			String line;

			while ((line = buffer.readLine()) != null) {
				User newUser = Converter.jsonToUser(line);
				list.add(newUser);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getFilePath() {
		// TODO: CHANGE THE PATH!!!
		return "filepath";
	}
	
	private ArrayList<User> getList() {
		return list;
	}

	private static final UsersList instance = new UsersList();
	private ArrayList<User> list;
}
