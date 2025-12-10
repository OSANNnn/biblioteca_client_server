package zekusan.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import zekusan.enums.Route;
import zekusan.interfaces.Navigator;
import zekusan.interfaces.PanelLifecycle;
import zekusan.services.LibraryClient;

public class SettingsPanel extends JPanel implements PanelLifecycle {
	private static final long serialVersionUID = 1L;

	private final transient LibraryClient libraryClient;
	private final transient Navigator navigator;
	private final JLabel statusLabel;
	private final JLabel userLabel;
	private final JLabel roleLabel;
	private final JLabel tokenLabel;

	public SettingsPanel(LibraryClient libraryClient, Navigator navigator) {
		super(new BorderLayout(8, 8));
		this.libraryClient = libraryClient;
		this.navigator = navigator;

		setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

		JLabel heading = new JLabel("Impostazioni", SwingConstants.LEFT);
		heading.setFont(heading.getFont().deriveFont(heading.getFont().getSize2D() + 4f));
		add(heading, BorderLayout.NORTH);

		JPanel card = new JPanel(new GridBagLayout());
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 200, 200)),
				BorderFactory.createEmptyBorder(12, 12, 12, 12)));

		statusLabel = new JLabel(" ");
		userLabel = new JLabel("-");
		roleLabel = new JLabel("-");
		tokenLabel = new JLabel("-");

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(4, 4, 4, 8);

		card.add(new JLabel("Stato:"), gbc);
		gbc.gridx = 1;
		card.add(statusLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		card.add(new JLabel("Utente:"), gbc);
		gbc.gridx = 1;
		card.add(userLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		card.add(new JLabel("Ruolo:"), gbc);
		gbc.gridx = 1;
		card.add(roleLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		card.add(new JLabel("Token:"), gbc);
		gbc.gridx = 1;
		card.add(tokenLabel, gbc);

		JPanel actions = new JPanel();
		var logout = new JButton("Logout");
		logout.addActionListener(e -> {
			libraryClient.logout();
			navigator.navigate(Route.LOGIN);
		});
		actions.add(logout);

		add(card, BorderLayout.CENTER);
		add(actions, BorderLayout.SOUTH);
	}

	@Override
	public void onShow() {
		if (libraryClient.isLoggedIn()) {
			var session = libraryClient.getSession();
			statusLabel.setText("Sessione attiva");
			userLabel.setText(session.username());
			roleLabel.setText(session.userType().name());
			tokenLabel.setText(String.valueOf(session.token()));
		} else {
			statusLabel.setText("Non autenticato");
			userLabel.setText("-");
			roleLabel.setText("-");
			tokenLabel.setText("-");
		}
	}

	@Override
	public void onHide() {
		// nothing to clean up
	}
}
