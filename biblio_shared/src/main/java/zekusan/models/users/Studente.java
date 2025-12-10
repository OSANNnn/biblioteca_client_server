package zekusan.models.users;

import zekusan.enums.UserType;

public class Studente extends User {
	public Studente() {
		super.setUserType(UserType.STUDENTE);
	}
}
