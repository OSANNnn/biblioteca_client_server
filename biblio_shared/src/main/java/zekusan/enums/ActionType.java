package zekusan.enums;

public enum ActionType {
	NONE,
	LOGIN,
	CATALOGO,
	PRENOTAZIONE;

	public static ActionType getType(String str) {
		switch (str) {
		case "login": return ActionType.LOGIN;
		case "catalogo": return ActionType.CATALOGO;
		case "prenotazione": return ActionType.PRENOTAZIONE;
		default: return ActionType.NONE;
		}
	}
}
