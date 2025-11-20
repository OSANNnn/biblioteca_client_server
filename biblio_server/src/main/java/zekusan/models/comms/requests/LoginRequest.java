package zekusan.models.comms.requests;

public class LoginRequest extends Request {
	public String getUsername() {
		return username;
	}

	public void setUsername(String user) {
		this.username = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	LoginRequest() {
	};

	String username;
	String password;
}
