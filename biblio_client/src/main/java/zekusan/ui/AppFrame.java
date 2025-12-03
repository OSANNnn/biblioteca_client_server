package zekusan.ui;

import java.awt.CardLayout;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import zekusan.enums.Route;
import zekusan.enums.Status;
import zekusan.enums.UserType;
import zekusan.comms.responses.LoginResponse;
import zekusan.routing.Router;
import zekusan.services.LibraryClient;
import zekusan.ui.components.LibrarianNavBar;
import zekusan.ui.components.StudentNavBar;
import zekusan.ui.views.CatalogPanel;
import zekusan.ui.views.LibrarianDashboardPanel;
import zekusan.ui.views.LoginPanel;
import zekusan.ui.views.NotFoundPanel;
import zekusan.ui.views.SettingsPanel;
import zekusan.ui.views.StudentDashboardPanel;

public class AppFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public AppFrame(LibraryClient libraryClient) {
		super("Biblioteca Client");

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(900, 600);
		setLocationRelativeTo(null);

		JPanel root = new JPanel(new CardLayout());
		Router router = new Router(root);

		router.register(Route.LOGIN, () -> new LoginPanel((username, password, onError) -> handleLogin(username, password, onError, router, libraryClient)));

		router.register(Route.STUDENT_DASHBOARD,
				() -> new PanelWithNav(new StudentDashboardPanel(libraryClient), new StudentNavBar(router)));
		router.register(Route.STUDENT_CATALOG,
				() -> new PanelWithNav(new CatalogPanel(libraryClient), new StudentNavBar(router)));
		router.register(Route.STUDENT_SETTINGS,
				() -> new PanelWithNav(new SettingsPanel(libraryClient, router), new StudentNavBar(router)));

		router.register(Route.LIBRARIAN_DASHBOARD,
				() -> new PanelWithNav(new LibrarianDashboardPanel(libraryClient), new LibrarianNavBar(router)));
		router.register(Route.LIBRARIAN_CATALOG,
				() -> new PanelWithNav(new CatalogPanel(libraryClient), new LibrarianNavBar(router)));
		router.register(Route.LIBRARIAN_SETTINGS,
				() -> new PanelWithNav(new SettingsPanel(libraryClient, router), new LibrarianNavBar(router)));
		router.register(Route.NOT_FOUND, NotFoundPanel::new);

		setContentPane(root);
		router.navigate(Route.LOGIN);
	}

	private boolean handleLogin(String username, String password, Consumer<String> onError, Router router,
			LibraryClient libraryClient) {
		if (!validateCredentials(username, password, onError)) {
			return false;
		}

		try {
			LoginResponse response = libraryClient.login(username, password);
			if (response.getStatus() != Status.SUCCESS) {
				onError.accept("Credenziali non valide");
				return false;
			}

			var session = libraryClient.getSession();
			Route target = session != null && session.userType() == UserType.STUDENTE
					? Route.STUDENT_DASHBOARD
					: Route.LIBRARIAN_DASHBOARD;
			SwingUtilities.invokeLater(() -> router.navigate(target));
			return true;
		} catch (IOException e) {
			onError.accept("Errore di connessione: " + e.getMessage());
			return false;
		}
	}

	private boolean validateCredentials(String username, String password, Consumer<String> onError) {
		if (username == null || username.isBlank()) {
			onError.accept("Username obbligatorio");
			return false;
		}
		if (password == null || password.isBlank()) {
			onError.accept("Password obbligatoria");
			return false;
		}
		return true;
	}
}
