package notguiserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

class Model {

	private ArrayList<Socket> clients = new ArrayList<>();
	private HashMap<String, Socket> hm = new HashMap<>();
	private ArrayList<String> nickNames = new ArrayList<>();

	public HashMap<String, Socket> getHashMap() {
		return hm;
	}

	public ArrayList<Socket> getSocketList() {
		return clients;
	}

	public ArrayList<String> getNickNames() {
		return nickNames;
	}

	public void connect(String nick, Socket soc) {
		hm.put(nick, soc);
		clients.add(soc);
		nickNames.add(nick);
	}

	public void disConnect(Socket soc, String nick) {
		hm.remove(soc);
		clients.remove(soc);
		nickNames.remove(nick);
	}

}
