package cs5300proj1b;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;


public class RPCServer extends Thread {
   
  DatagramSocket rpcSocket = null;
  int serverPort =5300;
   
   public RPCServer() {
      try {
    	  //Listen to 5300 port
    	  rpcSocket = new DatagramSocket(serverPort);
	  	       
      } catch (SocketException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
  public int getPort(){
	  return serverPort;
  }
	public void run()
	{
		while(true)
		{
			
			byte[] inBuf= new byte[512];
			byte[] outBuf= null;
			
			DatagramPacket recvPkt= new DatagramPacket(inBuf, inBuf.length);
		    
			try
			{
				System.out.println("In server");
				rpcSocket.receive(recvPkt);
				InetAddress returnAddr= recvPkt.getAddress();
				int returnPort= recvPkt.getPort();
				
				//Unmarshall the datagram packet 
				String[] packetList = new String(recvPkt.getData(),0,recvPkt.getLength()).split("@");
				
				//Take action based on the opcode
				outBuf=takeAction(packetList);
				
				DatagramPacket sendPkt= new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
				rpcSocket.send(sendPkt);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private byte[] takeAction(String[] dataContent)
	{
	    
		int operationType = Integer.parseInt(dataContent[1]);
		String response=null;
		
		if(operationType==1) //Session Read - Read from Hash Table
		{
			String callid = dataContent[0];
			String sessionid = dataContent[2];
			String version = dataContent[3];
			
			String[] Array= Session.session_data.get(sessionid);
			
			if(Array==null)
			{
				response=callid + "@";
			}
			else
			{
				response=callid+"@"+Array[0]+"@"+Array[1]+"@"+Array[2];
				System.out.println("Server side HT 1: "+ Array[0]+Array[1]+Array[2]);
			}
		}
		
		if(operationType==2) //Session Write - Write to Hash table
		{
			String callid = dataContent[0];
			String sessionid = dataContent[2];
			String version = dataContent[3];
			
			String message=dataContent[4];
			String expirationTime=dataContent[5];
			String[] Array = {message,version,expirationTime};
			System.out.println("Server side HT 2:"+ Array[0]+Array[1]+Array[2]);
			Session.session_data.put(sessionid, Array);
			
			response=callid + "@";
		}
		
		if(operationType==3) //Ping and return call id
		{
			String callid = dataContent[0] ;
			
			response=callid+ "@";
		}
			        
		System.out.println("Server responding with: " + response);
		//Marshall the response
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    ObjectOutput out;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    byte[] output = bos.toByteArray();
		
		return output;
	}
}
			