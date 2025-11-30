package zekusan.app.systems;

import java.util.HashMap;
import java.util.Map;

public class SessionSystem {
	
	public static boolean createSession(String user, int token) {		
		if (sessions.containsKey(user)) {
			System.out.println("[ERROR] Session for: " + user + " , already exists.");
			return false;
		}
		
		sessions.put(user, token);
		
		return true;
	}
	
	public static boolean validateSession(String user, int token) {		
		return sessions.get(user) == token;
	}
	
	public static void deleteSession(String user) {
		TokenSystem.deleteToken(sessions.get(user));
		sessions.remove(user);
		
	}
	
	private SessionSystem() {
		
	}

	private static Map<String, Integer> sessions = new HashMap<>();
}
