package application;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

class Model {

	private ArrayList<Socket> clients = new ArrayList<>();
	private HashMap<Socket, String> hm = new HashMap<>();
	private ArrayList<String> nicknames = new ArrayList<>();


	public HashMap<Socket, String> GetHashMap() {
		return hm;
	}

	public ArrayList<Socket> GetSocketList() {
		return clients;
	}

	public ArrayList<String> GetNickNames() {
		return nicknames;
	}
	public void Connect(Socket soc,String nick) {
		hm.put(soc, nick);
		clients.add(soc);
		nicknames.add(nick);
	}
	
	public void DisConnect(Socket soc, String nick) {
		hm.remove(soc);
		clients.remove(soc);
		nicknames.remove(nick);
	}

}
