package client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	
	static DataOutputStream data_out;
	static DataInputStream in;
	
	static String directoryName = "C:\\p2pfiles\\";
	
	public static void main(String[] args) {
		String serverName = "192.168.1.27";
		int port = 7734, rfc_count =0,rfcNumber;
		String title ="Demo RFC";
		int option;
		boolean exit = false;

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			System.out.println("Connecting to "+serverName +" on port "+port);
			// this is where the client is connecting to the central server
			Socket client = new Socket(serverName,port);
			System.out.println("Just connected to "+client.getRemoteSocketAddress());
			
			
			// start the peer upload connection
			// spawn PeerUpload
			Thread t = new Thread(new PeerUpload(directoryName));
			t.start();
			
			OutputStream out = client.getOutputStream();
			data_out = new DataOutputStream(out);
			data_out.writeUTF(client.getLocalAddress().toString());
			data_out.writeUTF(Integer.toString(client.getLocalPort()));
			System.out.println("How many RFC's do you have?");
			rfc_count = Integer.parseInt(reader.readLine());
			for(int i=0;i<rfc_count;i++)
			{
				System.out.println("Enter RFC number "+(i+1));
				rfcNumber = Integer.parseInt(reader.readLine());
				System.out.println("Enter RFC title");
				title = reader.readLine();
				data_out.writeUTF(rfcNumber+","+title);
			}
			data_out.writeUTF("end");
			InputStream inFromServer = client.getInputStream();
			in = new DataInputStream(inFromServer);
			String rfcNum,rfcTitle,versionNum;
			while(true)
			{
				System.out.println("");
				System.out.println(in.readUTF());
				System.out.println(in.readUTF());
				//option = standard_input.nextInt();
				option = Integer.parseInt(reader.readLine());
				//System.out.println(option);
				switch(option)
				{
				case 1: //Add
					System.out.println("Add option selected");
					System.out.println("Enter RFC number :");
					rfcNum = reader.readLine();
					//System.out.println("Enter version number ");
					//versionNum = reader.readLine();
					System.out.println("Enter RFC title");
					rfcTitle = reader.readLine();
				//	standard_input.close();
					data_out.writeUTF("ADD RFC "+rfcNum+" "+"P2P-CI/1.0");
					//System.out.println("ADD RFC "+rfc_num+" "+version_num);
					data_out.writeUTF("Host: "+client.getLocalAddress().toString());
					//System.out.println("Host: "+client.getLocalAddress().toString());
					data_out.writeUTF("Port: "+client.getLocalPort());
					//System.out.println("Port: "+client.getLocalPort());
					data_out.writeUTF("Title: "+rfcTitle);
					//System.out.println("Title: "+rfc_title);
					break;
					
				case 2: // Lookup
					System.out.println("Lookup option selected");
					System.out.println("Enter RFC number :");
					rfcNum = reader.readLine();
					//System.out.println("Enter version number :");
					//versionNum =reader.readLine();
					System.out.println("Enter RFC title :");
					rfcTitle = reader.readLine();
					data_out.writeUTF("LOOKUP RFC "+rfcNum+" "+"P2P-CI/1.0");
					//System.out.println("ADD RFC "+rfc_num+" "+version_num);
					data_out.writeUTF("Host: "+client.getLocalAddress().toString());
					//System.out.println("Host: "+client.getLocalAddress().toString());
					data_out.writeUTF("Port: "+client.getLocalPort());
					//System.out.println("Port: "+client.getLocalPort());
					data_out.writeUTF("Title: "+rfcTitle);
					System.out.println("");
					
					//Displaying input reader from server
					while(true)
					{
						String temp = in.readUTF();
						if(temp.equals("end"))
							break;
						System.out.println(temp);
					}
					break;
					
				case 3: // List
					System.out.println("List option selected");
					//System.out.println("Please enter version number :");
					//versionNum = reader.readLine();
					data_out.writeUTF("LIST ALL P2P-CI/1.0");
					data_out.writeUTF("Host: "+client.getLocalAddress());
					data_out.writeUTF("Port: "+client.getLocalPort());
					System.out.println("");
					while(true)
					{
						String temp = in.readUTF();
						if(temp.equals("end"))
							break;
						System.out.println(temp);
					}
					break;
				
				case 4: // download rfc file
					downloadFile(reader);
					System.out.println("i am outside download now");
					break;
					
				case 5: // Close connection
					data_out.writeUTF("end");
					exit = true;
					System.out.println(in.readUTF());
					
					client.close();
					System.exit(0);    
					break;
				}
				
				//Checking for  exit condition
				if(exit)
					break;
			}
			System.out.println(in.readUTF());
			client.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}

	private static void downloadFile(BufferedReader reader) throws IOException {

		String filePath = "C:\\p2pfiles\\";

		System.out.println("Enter RFC Number");
		String rfcFileNumber = reader.readLine();
		String rfcFileName = "rfc" + rfcFileNumber +".txt";
		
		System.out.println("Enter RFC Title");
		String rfcTitle = reader.readLine();
		
		System.out.println("Enter RFC Version");
		String versionNumber = reader.readLine();
		
		System.out.println("Enter Host Name");
		String hostName = reader.readLine();

		// System.out.println("Enter Port Number");
		// int portNumber = Integer.parseInt(reader.readLine());
		int portNumber = 7735;
		
		System.out.println("downloading begins now...");
		
		// check if the entered RFC number is available for download
		// TODO:: <do we need to check if the RFC is available or not??> 

		// if available establish a connection and download the file from the appropriate peer.

		System.out.println("Connecting to host " + hostName + " on port.");
		System.out.println("clients port number is "+portNumber);
		Socket peerClient = new Socket(hostName, portNumber);

		DataInputStream in = new DataInputStream(peerClient.getInputStream());
		DataOutputStream out = new DataOutputStream(peerClient.getOutputStream());

		String msgToSend = "GET RFC ";
		msgToSend += rfcFileNumber;
		msgToSend += " P2P-CI/"+versionNumber+" ";
		msgToSend += "Host: "+hostName+" ";
		msgToSend += "OS: " + System.getProperty("os.name");

		out.writeUTF(msgToSend);
		
		String response = in.readUTF();
		System.out.println(response);
		
		boolean addRFCDetails = false;
		
		if(response.indexOf("200 OK") != -1){
			byte[] b = new byte[1024];
			int len = 0;
			int bytcount = 1024;
			FileOutputStream inFile = new FileOutputStream(filePath + rfcFileName);
			InputStream peerInputStream = peerClient.getInputStream();
			BufferedInputStream pis = new BufferedInputStream(peerInputStream, 1024);
			while ((len = pis.read(b, 0, 1024)) != -1) {
				bytcount = bytcount + 1024;
				inFile.write(b, 0, len);
				System.out.println(new String(b));
			}
			
			peerInputStream.close();
			pis.close();
			inFile.close();
			
			addRFCDetails = true;
		}
		else if(response.indexOf("505 P2P-CI Version Not Supported") != -1){
			System.out.println("Bad version error");
		}
		else if(response.indexOf("404 Not Found") != -1){
			System.out.println("The peer is a liar. It does not have the file.");
		}
		else if(response.indexOf("400 Bad Request") != -1){
			System.out.println("Dude get the rfc details right");
		}
		
		in.close();
		out.close();
		peerClient.close();
		
		System.out.println("i am here right now");
		if(addRFCDetails){
			// send details to the server
			data_out.writeUTF("ADD RFC "+rfcFileNumber+" "+"P2P-CI/"+versionNumber);
			//System.out.println("ADD RFC "+rfc_num+" "+version_num);
			data_out.writeUTF("Host: "+peerClient.getLocalAddress().toString());
			//System.out.println("Host: "+client.getLocalAddress().toString());
			data_out.writeUTF("Port: "+peerClient.getLocalPort());
			//System.out.println("Port: "+client.getLocalPort());
			data_out.writeUTF("Title: "+rfcTitle);
		}
		else{
			data_out.writeUTF("Download");
		}
		System.out.println("i have just sent the details to the server");
	}
}
