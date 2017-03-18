package ie.gmit.sw;

/* This class provides a very simple implementation of a web server. As a web server
 * must be capable of handling multiple requests from web browsers at the same time,
 * it is essential that the server is threaded, i.e. that the web server can perform
 * tasks in parallel and serially (one request at a time, after another).
 * 
 * In programming languages, all network communication is handled using sockets. A 
 * socket is a software abstraction of a connection between one computer on a network
 * and another. A server-socket is a process that listens on a port number for 
 * incoming client requests. For example, the standard port number for a HTTP server (a
 * web server) is port 80. Most of the commonly used Java networking classes are 
 * available in the java.net package. The java.io package contains a set of classes
 * designed to handle Input/Output (I/O) activity. We will use both packages in the web
 * server class below.  
 */

import java.io.*; //Contains classes for all kinds of I/O activity
import java.net.*; //Contains basic networking classes
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileServer {
	private ServerSocket ss; //A server socket listens on a port number for incoming requests

	//The first 1024 ports require administrator privileges. We'll use portNo the user enter in command line  instead. The range 
	//of port numbers runs up to 2 ^ 16 = 65536 ports.
	private int SERVER_PORT = -1;
	private String FilePath = "";

	//The boolean value keepRunning is used to control the while loop in the inner class called
	//Listener. The volatile keyword tells the JVM not to cache the value of keepRunning during
	//optimisation, but to check it's value in memory before using it.
	private volatile boolean keepRunning = true;
	private BlockingQueue  logerQueue = new ArrayBlockingQueue(1024);


	//A null constructor for the WebServer class
	private FileServer(int portNo, String filesPath){
		this.SERVER_PORT = portNo;
		this.FilePath = FilePath;
		try { //Try the following. If anything goes wrong, the error will be passed to the catch block

			ss = new ServerSocket(portNo); //Start the server socket listening on server port 

			/* A Thread is a worker. A runnable is a job. We'll give the worker thread called "server"
			 * the job of handling incoming requests from clients.
			 * Note: calling start results in a new JVM stack being created. The run() method of the Thread
			 * or Runnable will be placed on the new stack and executed when the Thread Scheduler (consider
			 * this a cantankerous and uncommunicative part of the JVM) decides so. There is absolutely NO
			 * GUARANTEE of either order or execution time. We can however ask the Thread Scheduler 
			 * (politely) to run a thread as a max, min or normal priority. 
			 */
			Thread server = new Thread(new Listener(filesPath), "File Server Listener"); //We can also name threads
			server.setPriority(Thread.MAX_PRIORITY); //Ask the Thread Scheduler to run this thread as a priority
			server.start(); //The Hollywood Principle - Don't call us, we'll call you

			System.out.println("Server started and listening on port " + SERVER_PORT);

		} catch (IOException e) { //Something nasty happened. We should handle error gracefully, i.e. not like this...
			System.out.println("Yikes! Something bad happened..." + e.getMessage());
		}
	}

	//A main method is required to start a standard Java application
	public static void main(String[] args) {

		if( args.length != 2){
			System.out.println("Usage:java -cp oop.jar ie.gmit.sw.FileServer 7777 /path/to/myfiles");
			return ;
		}
		System.out.println("Server Started");
		int portNo = Integer.parseInt(args [0]);
		String filesPath = args[1];
		new FileServer( portNo, filesPath); //Create an instance of a WebServer. This fires the constructor of WebServer() above on the main stack 
	}



	/* The inner class Listener is a Runnable, i.e. a job that can be given to a Thread. The job that
	 * the class has been given is to intercept incoming client requests and farm them out to other
	 * threads. Each client request is in the form of a socket and will be handled by a separate new thread.
	 */
	private class Listener implements Runnable{ //A Listener IS-A Runnable
		private String filesPath = "";
		public Listener(String filesPath){
			this.filesPath = filesPath;
		}

		//The interface Runnable declare the method "public void run();" that must be implemented
		public void run() {
			int counter = 0; //A counter to track the number of requests
			Logger requestLoger  = new Logger (logerQueue);
			while (keepRunning){ //Loop will keepRunning is true. Note that keepRunning is "volatile"
				try { //Try the following. If anything goes wrong, the error will be passed to the catch block

					Socket s = ss.accept();
					
					//This is a blocking method, causing this thread to stop and wait here for an incoming request
					
					
					
					/* If we get to this line, it means that a client request was received and that the socket "s" is a real network
					 * connection between some computer and this programme. We'll farm out this request to a new Thread (worker), 
					 * allowing us to handle the next incoming request (we could have many requests hitting the server at the same time),
					 * so we have to be able to handle them quickly.
					 */
					ClientRequest connectRequest = new ClientRequest (ClientRequest.CONNECT, new Date(),s.getRemoteSocketAddress().toString());
					// log request
					
					logerQueue.put(connectRequest);

					new Thread(new ClientHandler(s, filesPath,logerQueue), "T-" + counter).start(); //Give the new job to the new worker and tell it to start work
					counter++; //Increment counter
				} catch (IOException e) { //Something nasty happened. We should handle error gracefully, i.e. not like this...
					System.out.println("Error handling incoming request..." + e.getMessage());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}//End of inner class Listener


	/* The inner class HTTPRequest is a Runnable, i.e. a job that can be given to a Thread. The job that
	 * the class has been given is to handle an individual client request, by reading information from the
	 * socket's input stream (bytes) and responding by sending information to the socket's output stream (more
	 * bytes).
	 */
	private class ClientHandler implements Runnable{
		private Socket sock; //A specific socket connection between some computer on a network and this programme
		private String filesPath;
		
		private BlockingQueue logerQueue = null;

		private ClientHandler(Socket request, String filesPath, BlockingQueue logerQueue) { //Taking the client socket as a constructor enables the Listener class above to farm out the request quickly
			this.sock = request; //Assign to the instance variable sock the value passed to the constructor.
			this.filesPath = filesPath;
			this.logerQueue = logerQueue;
		}

		//The interface Runnable declare the method "public void run();" that must be implemented
		public void run() {
			try{ //Try the following. If anything goes wrong, the error will be passed to the catch block

				//Read in the request from the remote computer to this programme. This process is called Deserialization or Unmarshalling
				ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
				Object command = in.readObject(); //Deserialise the request into an Object
				System.out.println("Read request from a cliet");
				System.out.println(command);

				//Write out a response back to the client. This process is called Serialization or Marshalling
				String message = "test response";
				ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
				out.writeObject(message);
				out.flush();
				String selection ="";
				while(selection.compareTo("4" )!= 0){
					
				
					//Read menu selection from client
					 selection = (String)in.readObject();
					System.out.println("Client sent menu selection: "+ selection);
					if( selection.compareTo("2")== 0){
						ClientRequest fileListRequest = new ClientRequest (ClientRequest.LISTFILES, new Date(),sock.getRemoteSocketAddress().toString());
						// Log request);
					
						// Log request
						logerQueue.put(fileListRequest);
						
						File folder = new File(filesPath);
						File[] fileList = folder.listFiles();
						// count files in folder
						int fileCount = 0;
						for(int i = 0; i < fileList.length; i++){
							if(fileList[i].isFile()){
								fileCount++;
							}
	
						}
						// Send file count to client 
						out.writeObject(fileCount);
						// loop sending each file name the client
						for(int i = 0; i < fileList.length; i++){
							if(fileList[i].isFile()){
								out.writeObject(fileList[i] .getName());
							}
						}
	
					}else if( selection.compareTo("3")== 0){
						// Read the requested file name
						String fileName = (String)in.readObject();
						String filePath = filesPath + "\\" + fileName;
						
						System.out.println("Received download request for: " + filePath);
						ClientRequest downloadRequest = new ClientRequest (ClientRequest.GETFILE, new Date(),sock.getRemoteSocketAddress().toString());
						
						// Log request
						logerQueue.put(downloadRequest);
						// Open file for reading data
						File targetFile = new File(filePath);
						int fileSize = (int)targetFile.length();
						InputStream fis = new FileInputStream(targetFile);
						int totalByteRead = 0;
						int byteRead = 0;
						int blockNumber = 0;
						// Use a block size 1024
						byte[] bytes = new byte[1024];
						// send file size to client
						out.writeObject(fileSize);
						
						// Read file block by block and send to client
						while((byteRead = fis.read(bytes,0, 1024)) > 0){
							//Update the total
							totalByteRead += byteRead;
							
							
							System.out.println("read " + byteRead + " bytes from file");
							System.out.println("totalByteRead: " + totalByteRead);
							// Send file data to client
							out.write(bytes, 0 ,byteRead);
							out.flush();
							blockNumber++;
							if(blockNumber * 1024  > fileSize){
								System.out.println("I finsh reading file data exiting loop");
								break;
							}
							
						}
						fis.close();
						
						ClientRequest fileListRequest = new ClientRequest (ClientRequest.CONNECT, new Date(),sock.getRemoteSocketAddress().toString());
						// Log request
	
					}

				}
				out.close(); //Tidy up after and don't wolf up resources unnecessarily

			} catch (Exception e) { //Something nasty happened. We should handle error gracefully, i.e. not like this...
				System.out.println("Error processing request from " + sock.getRemoteSocketAddress());
				e.printStackTrace();
			}
		}
private int calculateNextBlockSize(int byteRemaining ){
	int size = 1024 ;
	if (byteRemaining< 1024){
		
		
		size = byteRemaining;
		
		
		
	}
		System.out.println("nextBlockSize = "+ size ); 
		return size;
	
}
	}//End of inner class HTTPRequest
}//End of class WebServer