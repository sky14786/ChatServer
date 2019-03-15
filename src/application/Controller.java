package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class Controller {
	@FXML
	private TextArea TAdisplay;
	@FXML
	private Label Ltime;
	
	private long time = System.currentTimeMillis();
	private SimpleDateFormat daytime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
	private String nowtime = daytime.format(new Date(time));
	
	@FXML
	public void AddDisplay(String msg) {
		TAdisplay.appendText(msg);
	}
	
	@FXML
	public void NowTime() {
		Ltime.setText(nowtime);
	}
}



class RunThread extends Thread {

	
	private Controller controller = new Controller();
	private Model model;
	private Socket socket;
	private Boolean isduplicate;
	private DataOutputStream output;
	private DataInputStream input;
	private String nickname, sendmessage, receivemessage, identity;
	private HashMap<Socket, String> hm = new HashMap<>();
	private ArrayList<Socket> clients = new ArrayList<>();

	

	RunThread(Socket socket, Model model) {
		this.model = model;
		this.socket = socket;
		hm = model.GetHashMap();
		clients = model.GetSocketList();

	}

	public void run() {
		StringTokenizer temp;
		try {
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());

			temp = new StringTokenizer(input.readUTF(), ":");
			identity = temp.nextToken();
			if (identity.equals("0000")) {
				nickname = temp.nextToken();
				if (!DuplicateCheck()) {
					sendmessage = "1000:SERVER:서버 접속 성공!";
					output.writeUTF(sendmessage);
					output.flush();

					synchronized (this) {
						model.Connect(socket, nickname);
					}

					ReceiveMessage();
				} else {
					sendmessage = "1000:SERVER:서버 접속 실패 ID 중복 오류!";
					output.writeUTF(sendmessage);
					output.flush();
					socket.close();
				}
			}

		} catch (IOException e) {
			synchronized (this) {
				model.DisConnect(socket, nickname);
			}
		}
	}

	@FXML
	public void ReceiveMessage() {
		try {
			while (true) {
				receivemessage = input.readUTF();
				controller.AddDisplay(receivemessage + "\n");
				StringTokenizer temp = new StringTokenizer(receivemessage, ":");
				identity = temp.nextToken();
				if (identity.equals("1000")) {
					SendMessage(receivemessage);
				}
			}

		} catch (IOException e) {
			synchronized (this) {
				model.DisConnect(socket, nickname);
			}
		}
	}

	public void SendMessage(String msg) {
		sendmessage = msg;
		for (int j = 0; j < clients.size(); j++) {
			try {
				output = new DataOutputStream(clients.get(j).getOutputStream());
				output.writeUTF(sendmessage);
				output.flush();
			} catch (IOException e) {
				synchronized (this) {
					model.DisConnect(socket, nickname);
				}
			}
		}
	}

	public Boolean DuplicateCheck() {
		isduplicate = false;
		for (int i = 0; i < model.GetNickNames().size(); i++) {
			if (model.GetNickNames().get(i).equals(nickname)) {
				isduplicate = true;
				return isduplicate;
			} else {
				isduplicate = false;
			}
		}
		return isduplicate;
	}

	
}