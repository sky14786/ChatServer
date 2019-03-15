package notguiserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

class RunThread extends Thread {

	private Model model;
	private Socket socket;
	private Boolean isduplicate;
	private DataOutputStream output;
	private DataInputStream input;
	private String nickname, sendmessage, receivemessage, identity;
	private HashMap<String, Socket> hm = new HashMap<>();
	private ArrayList<Socket> clients = new ArrayList<>();

	private long time = System.currentTimeMillis();
	private SimpleDateFormat daytime = new SimpleDateFormat("yyyy-m-dd hh:mm:ss");
	private String nowtime = daytime.format(new Date(time));

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
					sendmessage = "1000:SERVER:���� ���� ����!";
					output.writeUTF(sendmessage);
					output.flush();

					synchronized (this) {
						model.Connect(nickname, socket);
					}

					ReceiveMessage();
				} else {
					sendmessage = "1000:SERVER:���� ���� ���� ID �ߺ� ����!";
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

	public void ReceiveMessage() {
		try {
			while (true) {
				receivemessage = input.readUTF();
				System.out.println("[" + NowTime() + "]" + receivemessage);
				StringTokenizer temp = new StringTokenizer(receivemessage, ":");
				identity = temp.nextToken();
				if (identity.equals("1000")) {
					SendMessage(receivemessage, identity);
				} else if (identity.equals("1001")) {
					String sender = temp.nextToken();
					String filename = temp.nextToken();
					FileReceiver(sender, filename);
				} else if (identity.equals("1100")) {
					String sender = temp.nextToken();
					String receiver = temp.nextToken();
					String msg = temp.nextToken();
					WhisperMessage(sender, receiver, msg);
				} else if (identity.equals("1101")) {
					String sender = temp.nextToken();
					String filename = temp.nextToken();
					String receiver = temp.nextToken();
					WhisperFileReceiver(sender, filename, receiver);
				}
			}

		} catch (IOException e) {
			synchronized (this) {
				model.DisConnect(socket, nickname);
			}
		}
	}

	public void SendMessage(String msg, String iden) {
		sendmessage = msg;
		if (iden.equals("1000")) {
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
		} else if (iden.equals("1001")) {
			for (int j = 0; j < clients.size(); j++) {
				try {
					if (clients.get(j) != hm.get(nickname)) {
						output = new DataOutputStream(clients.get(j).getOutputStream());
						output.writeUTF(iden + ":" + sendmessage);
						output.flush();
					}
				} catch (IOException e) {
					synchronized (this) {
						model.DisConnect(socket, nickname);
					}
				}
			}
		}
	}

	public void FileReceiver(String sender, String filename) {
		SendMessage(sender + ":" + filename, "1001");
		try {
			FileOutputStream fout = new FileOutputStream(filename);
			BufferedOutputStream buout;
			BufferedInputStream buin = new BufferedInputStream(socket.getInputStream());
			byte[] buffer = new byte[8096];
			int len;

			while ((len = buin.read(buffer)) != -1) {
				fout.write(buffer, 0, len);
				for (int i = 0; i < clients.size(); i++) {
					if (clients.get(i) != hm.get(nickname)) {
						buout = new BufferedOutputStream(clients.get(i).getOutputStream());
						buout.write(buffer, 0, len);
						buout.flush();
					}
				}
			}

			for (int i = 0; i < clients.size(); i++) {
				if (clients.get(i) != hm.get(nickname)) {
					buout = new BufferedOutputStream(clients.get(i).getOutputStream());
					buout.close();
				}
			}
			fout.flush();
			fout.close();
			buin.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public void WhisperFileReceiver(String sender, String filename, String receiver) {
		sendmessage = "1101:" + sender + ":" + filename + ":" + receiver;
		try {
			output = new DataOutputStream(hm.get(receiver).getOutputStream());
			output.writeUTF(sendmessage);
			output.flush();
			FileOutputStream fout = new FileOutputStream(filename);
			BufferedInputStream buin = new BufferedInputStream(socket.getInputStream());
			BufferedOutputStream buout = new BufferedOutputStream(hm.get(receiver).getOutputStream());
			int len;
			byte[] buffer = new byte[8096];

			while ((len = buin.read(buffer)) != -1) {
				fout.write(buffer, 0, len);
				buout.write(buffer, 0, len);
			}
			buout.flush();
			fout.flush();
			buout.close();
			buin.close();
			fout.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void WhisperMessage(String sender, String receiver, String msg) {
		sendmessage = "1100:" + sender + ":" + receiver + ":" + msg;
		try {
			output = new DataOutputStream(hm.get(receiver).getOutputStream());
			output.writeUTF(sendmessage);
			output.flush();
		} catch (IOException e) {
			synchronized (this) {
				model.DisConnect(socket, nickname);
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

	public String NowTime() {
		time = System.currentTimeMillis();
		nowtime = daytime.format(new Date(time));
		return nowtime;
	}

}