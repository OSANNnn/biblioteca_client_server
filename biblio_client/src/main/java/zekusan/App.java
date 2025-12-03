package zekusan;

import javax.swing.SwingUtilities;

import zekusan.net.ApiClient;
import zekusan.net.SocketClient;
import zekusan.services.LibraryClient;
import zekusan.ui.AppFrame;

public class App {
	private static final String DEFAULT_HOST = "0.0.0.0";
	private static final int DEFAULT_PORT = 1987;

	public static void main(String[] args) {
		SocketClient socketClient = new SocketClient(DEFAULT_HOST, DEFAULT_PORT);
		ApiClient apiClient = new ApiClient(socketClient);
		LibraryClient libraryClient = new LibraryClient(apiClient);

		SwingUtilities.invokeLater(() -> new AppFrame(libraryClient).setVisible(true));
	}
}
