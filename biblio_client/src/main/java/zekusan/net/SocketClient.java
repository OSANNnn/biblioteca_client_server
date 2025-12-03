package zekusan.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketClient {
	private final String host;
	private final int port;

	public SocketClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String send(String payload) throws IOException {
		if (payload == null) {
			throw new IllegalArgumentException("Payload cannot be null");
		}

		// try-with-resources Java 7 feature to auto-close 
		// socket, writer, and reader regardless of errors
		try (Socket socket = new Socket(host, port);
			 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

			writer.write(payload);
			writer.flush();
			socket.shutdownOutput(); // signal request end

			StringBuilder responseBuilder = new StringBuilder();
			char[] buffer = new char[1024];
			int read;
			while ((read = reader.read(buffer)) != -1) {
				responseBuilder.append(buffer, 0, read);
			}
			
			return responseBuilder.toString();
		}
	}
}
