package zekusan.services;

import zekusan.enums.UserType;

public record Session(String username, int token, UserType userType) {
	public boolean isValid() {
		return token > 0 && username != null && !username.isBlank();
	}
}
