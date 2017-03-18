package ie.gmit.sw;

import java.io.*; //We need the Java IO library to read from the socket's input stream and write to its output stream
import java.net.*; //Sockets are packaged in the java.net library
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;

public class FileClient { //The class WebClient must be declared in a file called WebClient.java
	//private static String downloadDirectory = "C:\\Users\\Abuchie\\Desktop\\OOP_FILE_SERVER\\download";
	private static Socket s = null;
	private static ObjectOutputStream out = null;
	private static ObjectInputStream in = null;
	private static Config config = null;
	private static void connect(int portno){
		try { //Attempt the following. If something goes wrong, the flow jumps down to catch()
			System.out.println("Run: attempting connection to server");
			s = new Socket(config.getServerHost(), config.getServerport()); //Connect to the server
			System.out.println("connected");



			//Serialise / marshal a request to the server
			out = new ObjectOutputStream(s.getOutputStream());
			System.out.println("connected output stream");


			// initialize output stream by sending data and flushing

			out.writeObject(new Date()); //Serialise
			out.flush(); //Ensure all data sent by flushing buffers
			in = new ObjectInputStream(s.getInputStream());
			System.out.println("connected intput stream");

			Thread.yield(); //Pause the current thread for a short time (not used much)


			//Deserialise / unmarshal response from server 

			String response = (String) in.readObject(); //Deserialise

			//Get the name of the thread (worker) doing this runnable (job)
			String threadName = Thread.currentThread().getName(); 
			System.out.println(response + "-->" + threadName );
		} catch (Exception e) { //Deal with the error here. A try/catch stops a programme crashing on error  
			System.out.println("Error: " + e.getMessage());
		}//End of try /catch

	}
	//Main method to get the ball rolling
	public static void main(String[] args) throws Throwable{

		System.out.println("Started client");
		// Display menu and get users selection
		 config = new Config ();

		try { 

			String first_Choice = "";
			while(first_Choice.compareTo("4") != 0){
				first_Choice = display_Client_Menu ();
				if(first_Choice.compareTo("1") == 0){
					// connect to server
					connect(7777);
				}else if(first_Choice.compareTo("2") == 0){
					// Get file listing
					System.out.println("Sending list file to server");
					out.writeObject(first_Choice);

					// Get number of files from server 

					int  numFiles = (int) in.readObject();
					//Read each file name from input stream
					String [] fileList = new String [numFiles];
					for(int i = 0; i < numFiles; i ++){
						fileList [i] = (String) in.readObject();
						System.out.println(fileList[i]);
					}
				}
				else if(first_Choice.compareTo("3") == 0){
					Scanner scanner = new Scanner(System.in) ;
					System.out.println("Please enter the file name");
					String fileName = scanner.nextLine();
					// Send a file download request to server

					out.writeObject(first_Choice);
					out.writeObject(fileName);
					// Read file size from server socket
					int fileSize = (int)in.readObject();
					System.out.println("Received file size: " + fileSize);
					if(fileSize<= 0){
						System.out.println("file not found");
					}else{
						
					
						int totalByteRead = 0 ;
						int nextBlock = 0;
						
						boolean createNewFile = true;
						Path p = Paths.get(config.getDownloadDirectory() + "\\"+fileName);
						if(Files.exists(p)){
							String userresponse = "";
							do{
								System.out.println("file alreay exist or do you want over write it (Y/N)");
								 userresponse = scanner.nextLine();
							}
							while(userresponse.equalsIgnoreCase("Y") == false && userresponse.equalsIgnoreCase("N")== false );
							if(userresponse.equalsIgnoreCase("N") == true){
								createNewFile = false;
							}
							
						}
						if(createNewFile){
							File file = new File(config.getDownloadDirectory() + "\\"+fileName);
							FileOutputStream fos = new FileOutputStream(file);
							file.createNewFile();
							// Loop, reading file data from server
							while(totalByteRead < fileSize ){
								
								// Prepare buffer to receive data
								byte[] fileBytes = new byte [1024];
		
								// Read the next block of data
								int byteRead = in.read(fileBytes);
								totalByteRead += byteRead ;
								System.out.println("read file data from server. " + byteRead +
										" bytes. totalBytes = "+ totalByteRead );
								// Write data to file
								System.out.println("Writing data to the file");
								fos.write(fileBytes,0,byteRead );
								
		
		
		
							}
							fos.flush();
							fos.close();
						}

						}
						
				}

			}


			s.close(); //Tidy up

		} catch (Exception e) { //Deal with the error here. A try/catch stops a programme crashing on error  
			System.out.println("Error: " + e.getMessage());
		}//End of try /catch



		System.out.println("Main method will return now....");


	}//End of main method

	static private String display_Client_Menu(){
		boolean valid = false;
		String choice = "";

		while(valid == false){
			System.out.println("Menu :");
			System.out.println("======");
			System.out.println("1 Connect to Server " );
			System.out.println("2 Print File Listing ");
			System.out.println("3 Download File");
			System.out.println("4 Quit ");
			System.out.println("");
			System.out.println("Enter your choice");
			Scanner scanner = new Scanner(System.in);
			choice = scanner.nextLine();
			if(choice.compareTo("1") != 0 && 
					choice.compareTo("2") != 0 &&
					choice.compareTo("3") != 0 && 
					choice.compareTo("4") != 0){
				System.out.println("Invalid option please try again");

			}
			else{
				valid = true;
			}

		}
		return choice;


	}
}//End of class
