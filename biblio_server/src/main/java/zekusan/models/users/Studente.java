package zekusan.models.users;

import zekusan.models.comms.UserType;

public class Studente extends User {
	public Studente() {
		super.setUserType(UserType.STUDENTE);
	}
}
