package notguiserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

class Model {

	private ArrayList<Socket> clients = new ArrayList<>();
	private HashMap<String, Socket> hm = new HashMap<>();
	private ArrayList<String> nicknames = new ArrayList<>();

	public HashMap<String, Socket> GetHashMap() {
		return hm;
	}

	public ArrayList<Socket> GetSocketList() {
		return clients;
	}

	public ArrayList<String> GetNickNames() {
		return nicknames;
	}

	public void Connect(String nick, Socket soc) {
		hm.put(nick, soc);
		clients.add(soc);
		nicknames.add(nick);
	}

	public void DisConnect(Socket soc, String nick) {
		hm.remove(soc);
		clients.remove(soc);
		nicknames.remove(nick);
	}

}
