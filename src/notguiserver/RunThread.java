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
	private Boolean isDuplicate;
	private DataOutputStream output;
	private DataInputStream input;
	private String nickName, sendMessage, receiveMessage, identity;
	private HashMap<String, Socket> hm = new HashMap<>();
	private ArrayList<Socket> clients = new ArrayList<>();

	private long time = System.currentTimeMillis();
	private SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-m-dd hh:mm:ss");
	private String nowTime = dayTime.format(new Date(time));

	RunThread(Socket socket, Model model) {
		this.model = model;
		this.socket = socket;
		hm = model.getHashMap();
		clients = model.getSocketList();

	}

	public void run() {
		StringTokenizer temp;
		try {
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());

			temp = new StringTokenizer(input.readUTF(), ":");
			identity = temp.nextToken();
			if (identity.equals("0000")) {
				nickName = temp.nextToken();
				if (!duplicateCheck()) {
					sendMessage = "1000:SERVER:서버 접속 성공!";
					output.writeUTF(sendMessage);
					output.flush();

					synchronized (this) {
						model.connect(nickName, socket);
					}

					receiveMessage();
				} else {
					sendMessage = "1000:SERVER:서버 접속 실패 ID 중복 오류!";
					output.writeUTF(sendMessage);
					output.flush();
					socket.close();
				}
			}

		} catch (IOException e) {
			synchronized (this) {
				model.disConnect(socket, nickName);
			}
		}
	}

	public void receiveMessage() {
		try {
			while (true) {
				receiveMessage = input.readUTF();
				System.out.println("[" + nowTime() + "]" + receiveMessage);
				StringTokenizer temp = new StringTokenizer(receiveMessage, ":");
				identity = temp.nextToken();
				if (identity.equals("1000")) {
					sendMessage(receiveMessage, identity);
				} else if (identity.equals("1001")) {
					String sender = temp.nextToken();
					String fileName = temp.nextToken();
					fileReceiver(sender, fileName);
				} else if (identity.equals("1100")) {
					String sender = temp.nextToken();
					String receiver = temp.nextToken();
					String msg = temp.nextToken();
					whisperMessage(sender, receiver, msg);
				} else if (identity.equals("1101")) {
					String sender = temp.nextToken();
					String fileName = temp.nextToken();
					String receiver = temp.nextToken();
					whisperFileReceiver(sender, fileName, receiver);
				}
			}

		} catch (IOException e) {
			synchronized (this) {
				model.disConnect(socket, nickName);
			}
		}
	}

	public void sendMessage(String msg, String iden) {
		sendMessage = msg;
		if (iden.equals("1000")) {
			for (int j = 0; j < clients.size(); j++) {
				try {
					output = new DataOutputStream(clients.get(j).getOutputStream());
					output.writeUTF(sendMessage);
					output.flush();
				} catch (IOException e) {
					synchronized (this) {
						model.disConnect(socket, nickName);
					}
				}
			}
		} else if (iden.equals("1001")) {
			for (int j = 0; j < clients.size(); j++) {
				try {
					if (clients.get(j) != hm.get(nickName)) {
						output = new DataOutputStream(clients.get(j).getOutputStream());
						output.writeUTF(iden + ":" + sendMessage);
						output.flush();
					}
				} catch (IOException e) {
					synchronized (this) {
						model.disConnect(socket, nickName);
					}
				}
			}
		}
	}

	public void fileReceiver(String sender, String fileName) {
		sendMessage(sender + ":" + fileName, "1001");
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			BufferedOutputStream buout;
			BufferedInputStream buin = new BufferedInputStream(socket.getInputStream());
			byte[] buffer = new byte[8096];
			int len;

			while ((len = buin.read(buffer)) != -1) {
				fout.write(buffer, 0, len);
				for (int i = 0; i < clients.size(); i++) {
					if (clients.get(i) != hm.get(nickName)) {
						buout = new BufferedOutputStream(clients.get(i).getOutputStream());
						buout.write(buffer, 0, len);
						buout.flush();
					}
				}
			}

			for (int i = 0; i < clients.size(); i++) {
				if (clients.get(i) != hm.get(nickName)) {
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

	public void whisperFileReceiver(String sender, String fileName, String receiver) {
		sendMessage = "1101:" + sender + ":" + fileName + ":" + receiver;
		try {
			output = new DataOutputStream(hm.get(receiver).getOutputStream());
			output.writeUTF(sendMessage);
			output.flush();
			FileOutputStream fout = new FileOutputStream(fileName);
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

	public void whisperMessage(String sender, String receiver, String msg) {
		sendMessage = "1100:" + sender + ":" + receiver + ":" + msg;
		try {
			output = new DataOutputStream(hm.get(receiver).getOutputStream());
			output.writeUTF(sendMessage);
			output.flush();
		} catch (IOException e) {
			synchronized (this) {
				model.disConnect(socket, nickName);
			}
		}

	}

	public Boolean duplicateCheck() {
		isDuplicate = false;
		for (int i = 0; i < model.getNickNames().size(); i++) {
			if (model.getNickNames().get(i).equals(nickName)) {
				isDuplicate = true;
				return isDuplicate;
			} else {
				isDuplicate = false;
			}
		}
		return isDuplicate;
	}

	public String nowTime() {
		time = System.currentTimeMillis();
		nowTime = dayTime.format(new Date(time));
		return nowTime;
	}

}