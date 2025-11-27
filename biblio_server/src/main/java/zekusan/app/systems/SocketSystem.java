package zekusan.app.systems;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketSystem {
	public boolean init() {
		status = false;
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("Couldn't start the server: " + e.getMessage());
			return status;
		}

		System.out.println("Server: started");
		System.out.println("Socket: " + serverSocket);
		clientSocket = null;
		bufferedReader = null;
		bufferedWriter = null;
		printWriter = null;
		inputStreamReader = null;
		outputStreamWriter = null;
		status = true;

		return status;
	}

	public void listen() throws IOException {
		clientSocket = serverSocket.accept();
		System.out.println("Connection accepted: " + clientSocket);

		inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
		outputStreamWriter = new OutputStreamWriter(clientSocket.getOutputStream());

		bufferedReader = new BufferedReader(inputStreamReader);
		bufferedWriter = new BufferedWriter(outputStreamWriter);

		printWriter = new PrintWriter(bufferedWriter, true);
	}

	public String getRequest() {
		StringBuilder sb = new StringBuilder();
		
        try {
            char[] buffer = new char[4096];
            int length = bufferedReader.read(buffer);
            
            if (length > 0) {
                sb.append(buffer, 0, length);
            }
        } catch (IOException e) {
            System.out.println("Error reading request: " + e.getMessage());
        }
        
        String json = sb.toString();
        
        return json;
	}

	public void sendResponse(String json) {
		if (json == null) {
			json = "NONE|{}";
		}
		
		if (printWriter != null) {
			printWriter.print(json);
			printWriter.flush();
		} else {
			System.out.println("Writer not initialized");
		}
	}
	
	public void closeConnection() {
		try {
			if (bufferedReader != null) bufferedReader.close();
			if (bufferedWriter != null) bufferedWriter.close();
			if (printWriter != null) printWriter.close();
			if (inputStreamReader != null) inputStreamReader.close();
			if (outputStreamWriter != null) outputStreamWriter.close();
			if (clientSocket != null) clientSocket.close();
		} catch (IOException e) {
			System.out.println("Error closing connection: " + e.getMessage());
		}
	}

	public void closeSocket() {
		try {
			closeConnection();
			if (serverSocket != null)
				serverSocket.close();
			status = false;
		} catch (IOException e) {
			System.out.println("Error closing server socket: " + e.getMessage());
		}
	}

	public boolean isOnline() {
		return status;
	}

	private static final int PORT = 1987;
	private boolean status;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private PrintWriter printWriter;
	private InputStreamReader inputStreamReader;
	private OutputStreamWriter outputStreamWriter;

}
