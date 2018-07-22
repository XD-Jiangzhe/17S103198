import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// Declaration filename, peerid and ipAddress
class FileIndex
{
	int peerid;
	String filename;
	String ipAddress;
}

class PortListener implements Runnable {

	public Boolean flag;
	public String strVal;
	public int port;
	static int maxsize = 0;
	// ArrayList Initialisation
	static FileIndex[] myIndexArray = new FileIndex[9000];

	public PortListener(int port) {
		this.port = port;
		this.flag = true;
		this.strVal = "Waiting For PEER Connection";
	}

	/* Beginning of Run Method */	
	public void run() {
		// Listening For Register on port 2001
		if (port == 2001) {
			try {
				ServerSocket server = new ServerSocket(2001);
				while (true) {
					Socket connection = server.accept();
					System.out.println("[OK] 	Connection received from " + connection.getInetAddress().getHostAddress()+ " for registration.");			 				   				
					ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
					strVal = (String)in.readObject();
					System.out.println("[ADD] 	Registrate " + strVal + " from " + connection.getInetAddress().getHostAddress()+ " successful.");
					// Split string "strVal" using Space as Delimeter store as {peerid ,filename} format;
					String[] var;
					var = strVal.split(" ");
					int aInt = Integer.parseInt(var[0]);
					String ipstrtmp = connection.getInetAddress().getHostAddress();
					/* print substrings */
					for(int x = 1; x < var.length ; x++){
						// Storing Peer ID and Filename in the ArrayList 
						FileIndex myitem = new FileIndex();
						myitem.filename = var[x];      
						myitem.peerid = aInt;
						myitem.ipAddress = ipstrtmp;
						myIndexArray[maxsize] = myitem;
						maxsize++;
					}
					in.close();
					connection.close();   				
				}
			}
			// To Handle Exceptions for Data Received in Unsupported/Unknown Formats
			catch(ClassNotFoundException noclass) {
				System.err.println("[ERROR]	Data Received in Unknown Format.");
			}
			// To Handle Input-Output Exceptions
			catch(IOException ioException) {
				ioException.printStackTrace();
			} 
			finally {
			}

		}
		// Listening for Search on port 2002
		if (port == 2002) {
			try {
				ServerSocket server = new ServerSocket(2002);
				while (true) {
					Socket connection = server.accept();			
					System.out.println("[OK]	Connection received from " + connection.getInetAddress().getHostAddress()+ " for search.");   				
					ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
					strVal = (String)in.readObject();
					String retval = "";
					// Peer-id's separated by space are returned for given file
					// Traversing the ArrayList
					for (int idx = 0; idx < maxsize ;idx++) { 
						// To Compare the filename with the Registered filenames in the ArrayList               
						if (myIndexArray[idx].filename.equals(strVal))
							// Returns the list of Peerid's which has the searched file
							retval = retval + "[INFO]	" + "FileIndex: " + myIndexArray[idx].peerid + " by register " + myIndexArray[idx].ipAddress + "\n";		
					}				
					if (retval == "") {
						retval = "[INFO]	File Not Found.\n";
					} 
					System.out.println("[INFO]	Request searching result:");
					System.out.print(retval);
					// Write the List of peer id's to the output stream
					ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
					out.flush();			
					out.writeObject(retval);
					out.flush();			
					in.close();
					out.close();
					connection.close();   				
				}
			} 
			// To Handle Exceptions for Data Received in Unsupported/Unknown Formats
			catch(ClassNotFoundException noclass) { 
				System.err.println("[ERROR]	Data Received in Unknown Format");
			}
			// To Handle Input-Output Exceptions
			catch(IOException ioException) {
				ioException.printStackTrace();
			} 
			finally {
			}
		}
		// Listening for Search on port 2003
		if (port == 2003) {
			try {
				ServerSocket server = new ServerSocket(2003);

				while (true) {
					Socket connection = server.accept();			
					System.out.println("[LIST]	Connection received from " + connection.getInetAddress().getHostAddress() + " for search all files.");    				   				
					String retval = "";
					// Peer-id's separated by space are returned for given file
					// Traversing the ArrayList
					for (int idx =0; idx < maxsize ;idx++)
						// Returns the list of Peerid's which has the searched file               			
						retval = retval + "[INFO]	" + "FileIndex: " + myIndexArray[idx].peerid + " by register " + myIndexArray[idx].ipAddress + "\n";
					if (retval == "") {
						retval = "[INFO]	File Not Found.\n";
					} 
					System.out.println("[INFO]	Request searching result:");
					System.out.print(retval);
					// Write the List of peer id's to the output stream
					ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
					out.flush();			
					out.writeObject(retval);
					out.flush();
					out.close();
					connection.close();   				
				}
			} 
			// To Handle Input-Output Exceptions
			catch(IOException ioException) {
				ioException.printStackTrace();
			} 
			finally {
			}
		}	
	}
}

/*CentralIndxServer Class Begin*/
public class CentralIndxServer {

	// RegisterRequest and SearchRequest Threads
	public CentralIndxServer() {
		RegisterRequestThread();
		SearchRequestThread();
		SearchAllRequestThread();
	}

	public static void main(String[] args) {


		@SuppressWarnings("unused")
		CentralIndxServer mainFrame = new CentralIndxServer();
	}

	public void RegisterRequestThread() {
		Thread rthread = new Thread(new PortListener(2001));
		rthread.setName("Listen For Register");
		rthread.start();
	}
	
	public void SearchRequestThread()
	{
		Thread sthread = new Thread(new PortListener(2002));
		sthread.setName("Listen For Search");
		sthread.start();

	}
	
	public void SearchAllRequestThread() {
		Thread sthread = new Thread(new PortListener(2003));
		sthread.setName("Listen For Search All");
		sthread.start();
	}
}
