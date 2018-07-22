import java.io.*;
import java.net.*;
import java.util.Scanner;

// PeerServer
class PortListenerSend implements Runnable {

	public int port;
	public String strVal;
	public Boolean flag;

	public PortListenerSend(int port) {
		this.port = port;
		this.flag = true;
		this.strVal = "Waiting For PEER Connection";
	}
	/* Beginning of Run Method */
	public void run() {
		try {
			ServerSocket server = new ServerSocket(port);
			// Listen for Download request
			// Write Files to Download Claient
			while (true) {
				Socket connection = server.accept();			
				System.out.println("[OK]	Connection Received From " + connection.getInetAddress().getHostAddress() + " For Download.");    				   				
				ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
				strVal = (String)in.readObject();
				ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();		
				String str = "";
				try {
					FileReader fr = new FileReader(strVal);
					BufferedReader br = new BufferedReader(fr);		
					String value = new String();
					while ((value = br.readLine()) != null)              
						str = str + value + "\r\n";                       
					br.close();
					fr.close();
				} catch (Exception e) {
					System.out.println("[ERROR]	Cannot Open File.");
				}
				out.writeObject(str);
				out.flush();
				in.close();
				connection.close();   				
			}
		} 
		catch (ClassNotFoundException noclass) {                                            
			System.err.println("[ERROR]	Data Received in Unknown Format.");
		}
		catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
		}
	}
}

/* PeerServer Class Begin */
public class PeerServer {
	// CIS means Central Index Server
	public String CIS_ip = "localhost";
	public String Clientid = "1001";
	public String regmessage, searchfilename;
	public ObjectOutputStream out;
	public Socket requestSocket;
	/* Main Method Begin */
	public static void main(String[] args) {

		@SuppressWarnings("unused")
		PeerServer psFrame = new PeerServer();

	}

	public PeerServer() {
		System.out.println("Enter The Option: ");
		System.out.println("add 	  -- 	Registering the File.");
		System.out.println("search 	  --	Searching Single File On CentralIndxServer.");
		System.out.println("list 	  --	Searcexhing All Files On CentralIndxServer.");
		System.out.println("download  --	Downloading From Peer Server.");
		System.out.println("exit 	  --	Exit Peer Client.");

		try {
			// read the filename in to filereader object
			FileReader fr = new FileReader("indxip.txt");
			BufferedReader br = new BufferedReader(fr);	
			String val_ip = br.readLine();
			System.out.println("[INFO]	Configuration Central Index Server by Reading indexip.txt.");
			System.out.println("[INFO]	IndexServer IP is: " + val_ip);
			CIS_ip = val_ip;
			br.close();
			fr.close();
		} catch(Exception e) {
			System.out.println("[ERROR]	Could not read indexserver ip from indxip.txt");
		}

		while (true) {	
			Scanner in = new Scanner(System.in);
			regmessage = in.nextLine();
			if (regmessage.equals("add")) {

				System.out.println("[INFO]	Enter the integer for File Index and File Names separated by Space: ");
				regmessage = in.nextLine();
				// To Collect peer/client id from input string
				String[] val = regmessage.split(" ");
				int PeerPort  = Integer.parseInt(val[0]);
				// Register Method call
				RegisterWithIServer();
				AttendFileDownloadRequest(PeerPort);
			}
			// Search Method call	
			if (regmessage.equals("search")) {
				SearchWithIServer();
			}
			// Search Method call
			if (regmessage.equals("list")) {
				SearchWithAllServer();
			}
			// Download Method call
			if (regmessage.equals("download")) {
				DownloadFromPeerServer(); 
			}
			if (regmessage.equals("exit")) {
				System.out.println("[INFO]	Exiting Peer Client.");
				System.exit(0);   		
			}
		}
	}
	// FileDownload Request Thread   
	public void AttendFileDownloadRequest(int peerid) {
		Thread dthread = new Thread(new PortListenerSend(peerid));
		dthread.setName("AttendFileDownloadRequest");
		dthread.start();
	}
	
	public void RegisterWithIServer() {
		try{
			//1. Creating a socket to connect to the server
			requestSocket = new Socket(CIS_ip, 2001);
			System.out.println("[OK]	Connected to Register on CentralIndxServer on port 2001 Successful.");
			//2. To Get Input and Output streams
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();			
			out.writeObject(regmessage);
			out.flush();
			System.out.println("[OK]	Registered " + regmessage + " Successfully.");
		}
		// To Handle Unknown Host Exception
		catch(UnknownHostException unknownHost) {
			System.err.println("[ERROR]	Cannot Connect to an Unknown Host.");
		}
		catch(IOException ioException) { 
			ioException.printStackTrace();
		} 
		finally {
			//4: Closing connection
			try {
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
	// Search on the CentralIndexServer Method
	public void SearchWithIServer() {
		try{
			System.out.println("[INFO]	Enter the File Name to Search: ");
			Scanner in1 = new Scanner(System.in);
			searchfilename = in1.nextLine();

			//1. Creating a socket to connect to the Index server
			requestSocket = new Socket(CIS_ip, 2002);
			System.out.println("[OK]	Connected to Search on CentralIndxServer on port 2002.");
			//2. To Get Input and Output streams
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();			
			out.writeObject(searchfilename);
			out.flush();
			ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream());
			String strVal = (String)in.readObject();
			//  For File Not Found Print Condition 
			if(strVal.equals("[INFO]	File Not Found.\n")) {
				System.out.println("[INFO]	FILE Does Not Exist.");
			}
			else {
				System.out.print("[INFO]	File: " + searchfilename + " found at peers:" + "\n" + strVal);     
			}		
		}
		catch(UnknownHostException unknownHost) {
			System.err.println("[ERROR]	Cannot Connect to an Unknown Host.");
		}
		catch(IOException ioException) {
			ioException.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			try {
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
	// Search All files on the CentralIndexServer Method
	public void SearchWithAllServer() {
		try {			
			//1. Creating a socket to connect to the Index server
			requestSocket = new Socket(CIS_ip, 2003);
			System.out.println("[OK]	Connected to Search on CentralIndxServer on port 2003.");
			//2. To Get Input and Output streams
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();			
			ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream());
			String strVal = (String)in.readObject();
			//  For File Not Found Print Condition 
			if  (strVal.equals("[INFO]	File Not Found.\n")) {

				System.out.println("[INFO]	FILE Does Not Exist.");
			}
			else {
				System.out.print("[INFO]	File list found at peers:" + "\n" + strVal);     
			}		

		}
		catch(UnknownHostException unknownHost) {                                           
			System.err.println("[ERROR]	Cannot Connect to an Unknown Host.");
		}
		catch(IOException ioException) {
			ioException.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			try {
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
	// Download Function Method 
	public void DownloadFromPeerServer() {
		Scanner in1 = new Scanner(System.in);
		System.out.println("Enter Peer id:");                        
		String peerid = in1.nextLine();
		System.out.println("Enter peer IP Address to download file:");
		String ipadrs = in1.nextLine();
		System.out.println("Enter the File Name to be Downloaded:");      
		searchfilename = in1.nextLine();

		int peerid1 = Integer.parseInt(peerid);
		try {
			//1. Creating a socket to connect to the Index server
			requestSocket = new Socket(ipadrs, peerid1);
			System.out.println("[OK]	Connected to peerid : " + peerid1);
			//2. To Get Input and Output streams
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();			
			out.writeObject(searchfilename);
			out.flush();
			ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream());
			String strVal = (String)in.readObject();
			System.out.println("[INFO]	" + searchfilename + " Downloaded Successful.");
			writeToFile(strVal);
		}
		catch(UnknownHostException unknownHost) {
			System.err.println("[ERROR]	Trying to connect to an unknown host.");
		}
		catch(IOException ioException) {
			System.err.println("[ERROR]	FILE not Found at the Following PEER.");
			DownloadFromPeerServer();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			try {
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	public void writeToFile(String s) {
		try {  
			String fname = searchfilename;
			FileWriter fw = new FileWriter(fname, true);
			fw.write(s);
			fw.close();

		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
