package notguiserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

	private static ServerSocket serverSocket;
	private static Socket socket;
	private static Model model = new Model();
	private static RunThread runThread;

	public static void main(String[] args) {
		try {
			serverSocket = new ServerSocket(8000);
			while (true) {
				socket = new Socket();
				socket = serverSocket.accept();
				if (socket.isConnected()) {
					runThread = new RunThread(socket, model);
					runThread.start();

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
