package zekusan.interfaces;

import zekusan.enums.Route;

public interface Navigator {
	void navigate(Route route);

	void back();
}
