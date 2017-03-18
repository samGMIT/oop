package ie.gmit.sw;

import java.util.Date;

public class ClientRequest {
	public  static final int CONNECT = 1;
	public static final int LISTFILES = 2;
	public static final int GETFILE = 3;
	
	private int type = 0;
	private String fileName = "";
	private Date timeStamp = null;
	private String hostName ="";
	
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public ClientRequest(int type, String fileName, Date timeStamp,String hostName) {
		super();
		this.type = type;
		this.fileName = fileName;
		this.timeStamp = timeStamp;
		this.hostName = hostName;
	}
	public ClientRequest(int type,  Date timeStamp,String hostName) {
		super();
		this.type = type;
		
		this.timeStamp = timeStamp;
		this.hostName = hostName;
	}
}
