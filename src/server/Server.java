package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import peers.Peer;
import peers.RFC;

public class Server{
	
	volatile static List<Peer> activePeers;
	
	volatile static List<RFC> rfcIndexes;
	
	private static ServerSocket serverSocket;
	
	public final static int PORT = 7734;
	
	public static void start() throws IOException{
		
		// starting the server
		InetAddress addr = InetAddress.getByName("192.168.108.1");
		serverSocket = new ServerSocket(PORT,1000,addr);
		
		activePeers = new LinkedList<Peer>();
		rfcIndexes = new LinkedList<RFC>();
		
		waitForConnections();
	}

	private static void waitForConnections() throws IOException {
		
		List<Thread> threads = new ArrayList<Thread>();
		int connections = 1;
		while(true){
			System.out.println("Waiting for client on port "+serverSocket.getLocalPort()+"...");
			Socket server = serverSocket.accept();
			Thread t = new Thread(new PeerConnection(server, connections++));
			t.start();
			threads.add(t);
			System.out.println("num of threads : "+threads.size()+"\n");
		}
	}

	public List<Peer> getActivePeers() {
		return activePeers;
	}

	public List<RFC> getRfcIndexes() {
		return rfcIndexes;
	}
	
	public static void joinSystem(String hostName, int portNumber, List<RFC> peerRFC){
		// create a new process which controls the actions of a peer
		Peer peer = new Peer(hostName, portNumber);
		activePeers.add(0, peer);
		
		for(RFC rfc : peerRFC){
			rfcIndexes.add(0, rfc);
		}
	}
	
	public static void leaveSystem(Peer peer) {
		Iterator iter;

		synchronized (activePeers) {
			iter = activePeers.listIterator();
			while (iter.hasNext()) {
				Peer p = (Peer) iter.next();
				if (p.getHostName().equals(peer.getHostName())) {
					iter.remove();
				}
			}
		}

		synchronized (rfcIndexes) {
			iter = rfcIndexes.listIterator();
			while (iter.hasNext()) {
				RFC r = (RFC) iter.next();
				if (r.getPeerHostName().equals(peer.getHostName())) {
					iter.remove();
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		start();
	}

	public static boolean findRFC(RFC rfc) {
		Iterator iter = rfcIndexes.listIterator();
		while (iter.hasNext()) {
			RFC r = (RFC) iter.next();
			if (r.getNumber() == rfc.getNumber()) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<String> getPeerList(RFC rfc) {
		ArrayList<String> response = new ArrayList<String>();
		
		for( RFC r : rfcIndexes) {
			if (r.getNumber() == rfc.getNumber()) {
				for (Peer p : activePeers) {
					if (p.getHostName().equals(r.getPeerHostName())) {
						String s = "RFC "+r.getNumber()+" "+r.getTitle()+" "+r.getPeerHostName()+" "+p.getPortNumber();
						response.add(s);
					}
				}
			}
		}
		
		return response;
	}
	
	public static ArrayList<String> getAllPeers() {
		ArrayList<String> response = new ArrayList<String>();
		
		for( RFC r : rfcIndexes) {
			for (Peer p : activePeers) {
				if (p.getHostName().equals(r.getPeerHostName())) {
					String s = "RFC "+r.getNumber()+" RFC "+r.getTitle()+" "+r.getPeerHostName()+" "+p.getPortNumber();
					response.add(s);
				}
			}
		}
		
		return response;
	}
	
	/**
	 * 
	 * @param hostName this is the host name unique to a peer in the central index
	 * @return
	 */
	public static RFC getPeerRFC(String hostName, int rfcNumber){
		RFC rfc = null;
		
		for(RFC r : rfcIndexes){
			if(r.getPeerHostName().equals(hostName) && rfc.getNumber() == rfcNumber){
				rfc = new RFC(r.getNumber(), r.getTitle(), r.getPeerHostName());
			}
		}
		
		return rfc;
	}
}
