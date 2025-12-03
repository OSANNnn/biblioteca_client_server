package zekusan.ui.views;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import zekusan.interfaces.PanelLifecycle;
import zekusan.services.LibraryClient;

public class StudentDashboardPanel extends JPanel implements PanelLifecycle {
	private static final long serialVersionUID = 1L;

	private final transient LibraryClient libraryClient;
	private final JLabel label;

	public StudentDashboardPanel(LibraryClient libraryClient) {
		setLayout(new BorderLayout());
		this.libraryClient = libraryClient;
		label = new JLabel("", SwingConstants.CENTER);
		add(label, BorderLayout.CENTER);
	}

	@Override
	public void onShow() {
		if (libraryClient.isLoggedIn()) {
			var session = libraryClient.getSession();
			label.setText("Benvenuto, Studente " + session.username() + "!");
		} else {
			label.setText("Accedi per iniziare.");
		}
	}

	@Override
	public void onHide() {
		// no-op
	}
}
