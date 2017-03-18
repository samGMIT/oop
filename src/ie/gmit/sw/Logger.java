package ie.gmit.sw;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.Date;
import java.text.*;


public class Logger {
	private BlockingQueue requestQueue;

	public Logger(BlockingQueue requestQueue) {
		super();
		this.requestQueue = requestQueue;
		new Thread(new QueueProcessor(requestQueue), "QueueProcessor" ).start(); 
		; 
		
		
		
	}
	private class QueueProcessor implements Runnable{
		private  String LOG_FILENAME = "clientRequests.log";
		private BlockingQueue requestQueue ; 
		
		

		private QueueProcessor( BlockingQueue requestQueue ) {
			
			this.requestQueue  = requestQueue;
			
		}

		//The interface Runnable declare the method "public void run();" that must be implemented
		public void run() {
			BufferedWriter bw = null;
			FileWriter fw = null;
			
			try {
				// file writter
				fw = new FileWriter(LOG_FILENAME,true);
				 bw = new BufferedWriter(fw);
				
				 // looping
				 boolean processing = true;
				 while(processing){
					 // takes the next request
					 ClientRequest request = (ClientRequest) requestQueue.take();
					 //converting date to string
					 String formatedDate =  new SimpleDateFormat("yyyy - MM -dd, hh:mm:ss") .format(request.getTimeStamp());
					 String requestMessage  = "";
					 switch(request.getType()){
					 // Type of request whether is connect or listing or download
					 case ClientRequest.CONNECT:
						 requestMessage = "connection by " +request.getHostName() + " @ "+ formatedDate;
						 break;
						 
					 case ClientRequest.LISTFILES: requestMessage = "fileListing Request by " + request.getHostName() + " @ "+ formatedDate;
						 break;
						  
					 case ClientRequest.GETFILE:
						 requestMessage = "DownloadRequest by " + request.getHostName() + " @ "+ formatedDate;
						 break;
					 }
					 // buffer writes out the out come
					 bw.write(requestMessage + "\n"); 
					 bw.flush();
					
				 }
			} catch (IOException e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				try{
					if (bw!= null){
						bw.close();
						
					}
					if(fw!= null){
						fw.close();
					}
				}
				 catch (IOException e) {
						
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
			
		}
		
	}
	
	

}
