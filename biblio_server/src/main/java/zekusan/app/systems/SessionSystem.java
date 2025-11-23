package zekusan.app.systems;

import java.util.Map;

public class SessionSystem {
	public SessionSystem getInstance() {
		return instance;
	}
	
	public int createSession(String user) {
		int token = TokenSystem.generateToken();
		sessions.put(user, token);
		
		return token;
	}
	
	public boolean checkSession(int token, String user) {
		
		return sessions.get(user) == token;
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
