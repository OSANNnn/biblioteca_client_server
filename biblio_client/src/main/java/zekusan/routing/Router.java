package zekusan.routing;

import java.awt.CardLayout;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.JPanel;

import zekusan.enums.Route;
import zekusan.interfaces.Navigator;
import zekusan.interfaces.PanelLifecycle;

public class Router implements Navigator {
	private final JPanel host;
	private final CardLayout layout;
	private final Map<Route, Supplier<JPanel>> factories = new EnumMap<>(Route.class);
	private final Map<Route, JPanel> cache = new EnumMap<>(Route.class);
	private final Deque<Route> history = new ArrayDeque<>();

	public Router(JPanel host) {
		this.host = host;
		this.layout = (CardLayout) host.getLayout();
	}

	public Router register(Route route, Supplier<JPanel> factory) {
		factories.put(route, factory);
		return this;
	}

	@Override
	public void navigate(Route route) {
		Route target = factories.containsKey(route) ? route : Route.NOT_FOUND;
		Route previousRoute = history.peekLast();

		if (previousRoute != target) {
			history.addLast(target);
		}

		JPanel panel = cache.computeIfAbsent(target, key -> {
			JPanel panelInstance = factories.get(key).get();
			host.add(panelInstance, key.name());
			return panelInstance;
		});

		if (previousRoute != null) {
			JPanel previousPanel = cache.get(previousRoute);
			if (previousPanel instanceof PanelLifecycle lifecycle) {
				lifecycle.onHide();
			}
		}

		layout.show(host, target.name());

		if (panel instanceof PanelLifecycle lifecycle) {
			lifecycle.onShow();
		}
	}

	@Override
	public void back() {
		if (history.size() <= 1) {
			return;
		}

		Route current = history.removeLast();
		Route previous = history.peekLast();

		if (previous == null) {
			return;
		}

		JPanel currentPanel = cache.get(current);
		if (currentPanel instanceof PanelLifecycle lifecycle) {
			lifecycle.onHide();
		}

		layout.show(host, previous.name());

		JPanel previousPanel = cache.get(previous);
		if (previousPanel instanceof PanelLifecycle lifecycle) {
			lifecycle.onShow();
		}
	}
}
