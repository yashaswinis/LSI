package cs5300proj1b;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Session implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sessionID;
	public static Integer sessionNumber = 1;
	private String versionNumber;
	private String locationMetadata;
	private String message;
	private long expirationTime;
	static final int minutes = 1;
	
	//Concurrent hash map is used to make it threadsafe 
    static Map<String, String[]> session_data = new ConcurrentHashMap<String, String[]>();
    
	public Session(String sessionID) {
		this.sessionID = sessionID;
		this.versionNumber = "0";
		this.locationMetadata = "0";
		this.expirationTime = (new Date().getTime() + ((minutes * 60) * 1000)); //expiration time in milliseconds
		this.message = "Hello, User!!";
	}
    
	public void setSID(String sessionID) {
	    this.sessionID = sessionID;
	}

    public String getSID() {
	    return sessionID;
	}
    
    public String getVersion() {
	   return versionNumber;
	}
	   
	public void setVersion(String version){
	   this.versionNumber = version;
	}
	
	public String getlocationMetadata() {
		   return locationMetadata;
	}
		   
	public void setlocationMetadata(String locationMetadata){
		   this.locationMetadata = locationMetadata;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
	
	public void setExpirationTime(long l){
		this.expirationTime = l;
	}
	
	public long getExpirationTime(){
		return expirationTime;
	}
}