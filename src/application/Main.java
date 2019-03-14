package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Main extends Application {
	private static ServerSocket serversocket;
	private static Socket socket;
	private static Model model = new Model();
	private static RunThread runthread;

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("design.fxml"));
			primaryStage.setScene(new Scene(root));
			primaryStage.setTitle("[SERVER]");
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);

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
