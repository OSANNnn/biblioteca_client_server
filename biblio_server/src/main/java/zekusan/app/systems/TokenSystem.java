package zekusan.app.systems;

import java.util.Random;
import java.util.Set;

public class TokenSystem {
	public static int generateToken() {
		
		return TokenSystem.getInstance().generate();
	}
	
	public static void deleteToken (int token) {
		TokenSystem.getInstance().delete(token);
	}
	
	private int generate() {
		boolean isUnique = false;
		int token;
		
		do {
			token = Math.abs(generator.nextInt());
			
			if (!inUse.contains(token)) {
				isUnique = true;
				inUse.add(token);
			}
		} while (!isUnique);
		
		
		return token;
	}
	
	private void delete(int token) {
		inUse.remove(token);
	}
	
	private static TokenSystem getInstance() {
		return instance;
	}
	
	private TokenSystem() {};
	
	private Random generator = new Random();
	private static TokenSystem instance = new TokenSystem();
	private Set<Integer> inUse;
}
