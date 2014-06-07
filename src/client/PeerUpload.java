package client;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PeerUpload implements Runnable {

	Integer port = 7735;
	Socket peerSocket;
	String filePath = "";
	DataInputStream in = null;
	DataOutputStream out = null;

	public PeerUpload(String downloadPath) {
		this.filePath = downloadPath;
	}

	@Override
	public void run() {
		try {
			System.out.println("starting on port number "+port);
			ServerSocket downloadSocket = new ServerSocket(port);
			
			System.out.println("starting client socket now");
			while (true) {
				peerSocket = downloadSocket.accept();
				String statusCode = " ";
				DateFormat dateFormat = new SimpleDateFormat(
						"E, d MMM y HH:mm:ss z");
				Date date = new Date();

				out = new DataOutputStream(peerSocket.getOutputStream());
				in = new DataInputStream(peerSocket.getInputStream());
				
				System.out.println("resulting is printing here");
				String responseMsg = in.readUTF();
				String[] result = responseMsg.split(" ");
				String requestType = result[0];
				String rfcNumber = result[2];
				String versionInfo = result[3];
				System.out.println("-------");
				System.out.println(responseMsg);
				System.out.println("-------");
				System.out.println(result[0] + " " + result[1] + " "
						+ result[2] + " " + result[3]);
				System.out.println(result[4] + " " + result[5]);
				System.out.println(result[6] + " " + result[7]);

				File f = new File(filePath + "rfc"+rfcNumber+".txt");
				boolean fileExists = false;
				
				// RFC rfc = Server.getPeerRFC(peerSocket.getLocalSocketAddress().toString(), Integer.parseInt(rfcNumber));
				
				if (!f.exists()) {
					statusCode = "404 Not Found";
				}
				else if(!requestType.equals("GET")){
					statusCode = "400 Bad Request";
				}
				else if(versionInfo.indexOf("1.0") == -1){
					statusCode = "505 P2P-CI Version Not Supported";
				}
				else{
					// you can transmit the file if you get here
					statusCode = "200 OK";
					fileExists = true;
				}


				String sendResponse = "P2P-CI/1.0 ";

				sendResponse += statusCode + "\n";
				
				if(fileExists){
					
					OutputStream os = peerSocket.getOutputStream();
					byte[] buf = new byte[1024];
					BufferedOutputStream bout = new BufferedOutputStream(os, 1024);
					FileInputStream fin = new FileInputStream(f);
					
					int i = 0;
					sendResponse += "Date: " + dateFormat.format(date) + "\n";
					sendResponse += "OS: " + System.getProperty("os.name") + "\n";
					sendResponse += "Last-Modified: "
							+ dateFormat.format(f.lastModified()) + "\n";
					sendResponse += "Content-length: " + f.length() + "\n";
					sendResponse += "Content-type: " + "text/text\n";

					out.writeUTF(sendResponse);
					
					while ((i = fin.read(buf, 0, 1024)) != -1) {
						bout.write(buf, 0, i);
						bout.flush();
					}
					peerSocket.shutdownOutput();
					
					
					bout.close();
					fin.close();
				}
				else{
					// without a file you just send a response message. no file to download
					out.writeUTF(sendResponse);	
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
