package notguiserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

	private static ServerSocket serversocket;
	private static Socket socket;
	private static Model model = new Model();
	private static RunThread runthread;

	public static void main(String[] args) {
		try {
			serversocket = new ServerSocket(8000);
			while (true) {
				socket = new Socket();
				socket = serversocket.accept();
				if (socket.isConnected()) {
					runthread = new RunThread(socket, model);
					runthread.start();

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
