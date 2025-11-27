package zekusan.app.systems;

import java.util.Map;

public class SessionSystem {
	
	public static boolean createSession(String user, int token) {		
		if (instance.sessions.containsKey(user)) {
			System.out.println("[ERROR] Session for: " + user + " , already exists.");
			return false;
		}
		
		instance.sessions.put(user, token);
		
		return true;
	}
	
	public static boolean validateSession(String user, int token) {		
		return instance.sessions.get(user) == token;
	}
	
	public void deleteSession(String user) {
		TokenSystem.deleteToken(sessions.get(user));
		sessions.remove(user);
		
	}
	
	private SessionSystem() {
		
	}

	private Map<String, Integer> sessions;
	private static final SessionSystem instance = new SessionSystem();
}
