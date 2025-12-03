package zekusan.ui.views;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import zekusan.services.LibraryClient;
import zekusan.interfaces.PanelLifecycle;
import zekusan.interfaces.Navigator;
import zekusan.enums.Route;

public class SettingsPanel extends JPanel implements PanelLifecycle {
	private static final long serialVersionUID = 1L;

	private final transient LibraryClient libraryClient;
	private final transient Navigator navigator;
	private final JLabel statusLabel;

	public SettingsPanel(LibraryClient libraryClient, Navigator navigator) {
		super(new BorderLayout());
		this.libraryClient = libraryClient;
		this.navigator = navigator;

		statusLabel = new JLabel(" ");

		var logout = new JButton("Logout");
		logout.addActionListener(e -> {
			libraryClient.logout();
			navigator.navigate(Route.LOGIN);
		});

		add(logout, BorderLayout.NORTH);
		add(statusLabel, BorderLayout.SOUTH);
	}

	@Override
	public void onShow() {
		statusLabel.setText(libraryClient.isLoggedIn() ? "Sessione attiva" : "Non autenticato");
	}

	@Override
	public void onHide() {
		// nothing to clean up
	}
}
