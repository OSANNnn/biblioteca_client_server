package zekusan.ui.views;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LoginPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	@FunctionalInterface
	public interface LoginHandler {
		boolean handle(String username, String password, Consumer<String> onError);
	}

	private final transient LoginHandler loginHandler;
	private final JTextField usernameField;
	private final JPasswordField passField;
	private final JLabel errorLabel;
	private final JButton loginBtn;

	public LoginPanel(LoginHandler onLogin) {
		this.loginHandler = onLogin;

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

		loginBtn = new JButton("Login");

		loginBtn.addActionListener(event -> doLogin());
		usernameField.addActionListener(event -> doLogin());
		passField.addActionListener(event -> doLogin());

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

	@Override
	public void addNotify() {
		super.addNotify();
		SwingUtilities.invokeLater(() -> {
			var rootPane = SwingUtilities.getRootPane(this);
			if (rootPane != null) {
				var inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				var actionMap = rootPane.getActionMap();

				inputMap.put(KeyStroke.getKeyStroke("ENTER"), "login");
				actionMap.put("login", new AbstractAction() {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(java.awt.event.ActionEvent e) {
						doLogin();
					}
				});
			}
		});
	}

	private void doLogin() {
		var username = usernameField.getText();
		var password = new String(passField.getPassword());

		var loggedIn = loginHandler.handle(username, password, errorLabel::setText);

		if (loggedIn) {
			clearFields();
		}
	}
}
