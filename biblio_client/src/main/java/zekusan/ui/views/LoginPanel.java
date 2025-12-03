package zekusan.ui.views;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LoginPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	@FunctionalInterface
	public interface LoginHandler {
		boolean handle(String username, String password, Consumer<String> onError);
	}

	private final JTextField usernameField;
	private final JPasswordField passField;
	private final JLabel errorLabel;

	public LoginPanel(LoginHandler onLogin) {
		setLayout(new GridBagLayout());
		var c = new GridBagConstraints();
		c.insets = new Insets(6, 6, 6, 6);
		c.fill = GridBagConstraints.HORIZONTAL;

		usernameField = new JTextField(15);
		passField = new JPasswordField(15);
		errorLabel = new JLabel(" ");
		errorLabel.setForeground(Color.RED);

		var errorListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				clearError();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				clearError();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				clearError();
			}
		};

		usernameField.getDocument().addDocumentListener(errorListener);
		passField.getDocument().addDocumentListener(errorListener);

		var loginBtn = new JButton("Login");
		loginBtn.addActionListener(event -> {
			var username = usernameField.getText();
			var password = new String(passField.getPassword());

			var loggedIn = onLogin.handle(username, password, msg -> errorLabel.setText(msg));

			if (loggedIn) {
				clearFields();
			}
		});

		c.gridx = 0;
		c.gridy = 0;
		add(new JLabel("Username"), c);
		c.gridx = 1;
		add(usernameField, c);
		c.gridx = 0;
		c.gridy = 1;
		add(new JLabel("Password"), c);
		c.gridx = 1;
		add(passField, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		add(loginBtn, c);
		c.gridy = 3;
		add(errorLabel, c);
	}

	public void clearFields() {
		usernameField.setText("");
		passField.setText("");
		clearError();
	}

	private void clearError() {
		errorLabel.setText(" ");
	}
}
