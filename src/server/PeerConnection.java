package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import peers.Peer;
import peers.RFC;

public class PeerConnection implements Runnable{
	
	private Socket clientSocket;
	List<Peer> activePeers;
	List<RFC> rfcIndexes;
	int connectionNumber;
	
	DataInputStream in;
	DataOutputStream out;
 	
	public PeerConnection(Socket socket, int connectionNumber) throws IOException{
		
		this.connectionNumber = connectionNumber;
		this.clientSocket = socket;

		// get the input and output stream
		in = new DataInputStream(clientSocket.getInputStream());
		out = new DataOutputStream(clientSocket.getOutputStream());
		
		activePeers = Server.activePeers;
		rfcIndexes = Server.rfcIndexes;
		
		
	}
	
	Peer peer;
	
	@Override
	public void run() {
		try{
			System.out.println("Just connected to "+clientSocket.getRemoteSocketAddress());
			
			String host = in.readUTF();
			int port = Integer.parseInt(in.readUTF());
			
			peer = new Peer(host, port);
			
			List<RFC> rfcList = new LinkedList<RFC>();
			
			// rfc's are read here
			while(true){
				
				String input = in.readUTF();
				System.out.println(input);
				
				if(input.equals("end")){
					break;
				}
				
				// add rfc to the indexes list
				String[] temp = input.split(",");
				RFC rfc = new RFC(Integer.parseInt(temp[0]),temp[1],host);
				rfcList.add(rfc);
			}
			
			Server.joinSystem(host, port, rfcList);
			
			System.out.println(host +" on "+port+" has "+rfcList.size()+" rfc's added");
			System.out.println("connection number : "+connectionNumber);
			
			presentOptionsToPeer(host);
			
			out.writeUTF("Thank you for connecting to "+clientSocket.getLocalSocketAddress() +"\nBye");
			clientSocket.close();
		}
		catch(SocketTimeoutException s)
		{
			System.out.println("Socket timed out");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	private void presentOptionsToPeer(String host) throws IOException {
		boolean exit = false;
		
		while(true){
			out.writeUTF("following functionalities are available with the server:\n");
			out.writeUTF("1. ADD\n"
					+ "2. LOOKUP\n"
					+ "3. LIST\n"
					+ "4. Download\n"
					+ "5. End\n");
			
			// reading the option header
			String input = in.readUTF();
			System.out.println(input);
			String[] temp = input.split(" ");
			
			int rfcNum, resSize;
			String version, title;
			RFC rfc;
			
			ArrayList<String> response;
			
			switch (temp[0].toLowerCase()) {
			// add rfc to the indexes list
			case "add":
				
				rfcNum = Integer.parseInt(temp[2]);
				version = temp[3];
				
				// System.out.println("version - "+temp[3]);
				// reading the host header
				input = in.readUTF();
				System.out.println(input);
				// temp = input.split(" ");
				// String host = temp[1];
				
				// reading the port header
				input = in.readUTF();
				System.out.println(input);
				// temp = input.split(" ");
				// int port = Integer.parseInt(temp[1]);
				
				// reading the title header
				input = in.readUTF();
				temp = input.split(" ");
				title = temp[1];
				System.out.println(input);
				
				rfc = new RFC(rfcNum, title, host);
				
				// adding the rfc to the central index list
				rfcIndexes.add(0, rfc);
				
				System.out.println("added rfc now");
				break;
			
			case "lookup":
				
				rfcNum = Integer.parseInt(temp[2]);
				version = temp[3];
				
				// System.out.println("version - "+temp[3]);
				// reading the host header
				input = in.readUTF();
				System.out.println(input);
				// temp = input.split(" ");
				// String host = temp[1];
				
				// reading the port header
				input = in.readUTF();
				System.out.println(input);
				// temp = input.split(" ");
				// int port = Integer.parseInt(temp[1]);
				
				// reading the title header
				input = in.readUTF();
				temp = input.split(" ");
				title = temp[1];
				System.out.println(input);
				
				rfc = new RFC(rfcNum, title, host);
				
				// find if rfc exists
				// boolean rfcExists = Server.findRFC(rfc);
				response = Server.getPeerList(rfc);
				resSize = response.size();
				
				if(resSize != 0){
					out.writeUTF("P2P-CI/1.0 200 OK");
					for(String s : response){
						out.writeUTF(s);
					}
				}
				else{
					out.writeUTF("P2P-CI/1.0 404 Not Found");
				}
				
				out.writeUTF("end");
				
				break;
				
			case "list":
				// type = all
				String type = temp[1];
				version = temp[2];
				
				// System.out.println("version - "+temp[3]);
				// reading the host header
				input = in.readUTF();
				System.out.println(input);
				// temp = input.split(" ");
				// String host = temp[1];
				
				// reading the port header
				input = in.readUTF();
				System.out.println(input);
				// temp = input.split(" ");
				// int port = Integer.parseInt(temp[1]);
				
				response = Server.getAllPeers();
				resSize = response.size();
				
				if(resSize != 0){
					out.writeUTF("P2P-CI/1.0 200 OK");
					for(String s : response){
						out.writeUTF(s);
					}
				}
				else{
					out.writeUTF("P2P-CI/1.0 404 Not Found");
				}
				
				out.writeUTF("end");
				
				break;
				
			case "download":
				break;
				
			case "end":
				out.writeUTF("server was asked to close the connection. Adios\n");
				// remove the client rfc's from the central index
				Server.leaveSystem(peer);
				exit = true;
				break;
				
			default:
				System.out.println("exiting now");
				break;
			}
			
			if(exit)
				break;
		}
	}
}
