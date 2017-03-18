package ie.gmit.sw;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.io.File;

public class Config {
	private static String CONFIGFILE = "config.xml";
	private String username;
	private String serverHost;
	private int serverport;
	private String downloadDirectory;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getServerHost() {
		return serverHost;
	}
	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}
	public int getServerport() {
		return serverport;
	}
	public void setServerport(int serverport) {
		this.serverport = serverport;
	}
	public String getDownloadDirectory() {
		return downloadDirectory;
	}
	public void setDownloadDirectory(String downloadDirectory) {
		this.downloadDirectory = downloadDirectory;
	}
	public Config(String username, String serverHost, int serverport, String downloadDirectory) {
		super();
		this.username = username;
		this.serverHost = serverHost;
		this.serverport = serverport;
		this.downloadDirectory = downloadDirectory;
	}
	public Config(){
		parse();
	}
	private void parse(){
		try {
			//prepare to parse xml config file
			File config_file = new File(CONFIGFILE);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(config_file);

			//parse user name
			NamedNodeMap atts =doc.getDocumentElement().getAttributes();
			Node userNode = atts.getNamedItem("username");
			String userName = userNode.getNodeValue();


			System.out.println("username :" + userName);
			
			this.serverHost = parseConfigValue("server-host",doc);
			this.downloadDirectory = parseConfigValue("download-dir",doc);
			String configPort = parseConfigValue("server-port", doc);
			this.serverport = Integer.parseInt(configPort);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//looking for tag name and returning text content
	private String parseConfigValue(String tagName, Document doc){
		NodeList nList = doc.getElementsByTagName(tagName);
		if(nList.getLength() ==1){
			Node node = nList.item(0);
			
			System.out.println( "parseConfigValue: "+tagName +": "+ node.getTextContent());
			return node.getTextContent();

		}
		return null;
	}

}



