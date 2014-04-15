package cs5300proj1b;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;

public class NewGroupMembership extends Thread {
	public static final int gossipSecs = 10;  //Time interval between checks for view and update simple DB 
	public static final String simpleDBDomain = "Project1"; //Simple DB database which will have the bootstrap view

	AmazonSimpleDB sdb;
	Server current;
	static Random r = new Random();
	Views serverView;

	public NewGroupMembership(Server s) throws IOException {
		
		// Create a view for the current server
		current = s;
		serverView = new Views();  
		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				NewGroupMembership.class
						.getResourceAsStream("/AwsCredentials.properties")));
		
		// Add to SimpleDB
		List<ReplaceableAttribute> replaceableAttributes = new ArrayList<ReplaceableAttribute>();
		replaceableAttributes.add(new ReplaceableAttribute("ip", current.ip
				.getHostAddress(), true));
		System.out.println("Adding to simple DB in constructor"
				+ current.ip.getHostAddress());
		sdb.putAttributes(new PutAttributesRequest(simpleDBDomain, current
				.toString(), replaceableAttributes));
		
		//Call bootstrap method to pull the Server IPs registered in the Simple DB
		bootstrapSimpleDB(s);
	}

	public void run() {
		// Check every 0.5*GossipSecs to 1.5*GossipSec
		while (true) {
			try {
				Thread.sleep((int) ((r.nextDouble() + 0.5)
						* gossipSecs * 1000));
				
				//Update the current server's view
				bootstrapSimpleDB(current);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	
	public void bootstrapSimpleDB(Server sid) {

		// Get the list of server IPs 
		SelectRequest selectRequest = new SelectRequest("select * from "
				+ simpleDBDomain);
		
		Views temp = new Views();
		Views db = new Views();
		for (Item item : sdb.select(selectRequest).getItems()) {
			System.out.println("In update SimpleDB selecting server "
					+ item.getName());
			String addr = item.getName();
			try {
				temp.insert((new Server(InetAddress.getByName(addr))));
				db.insert((new Server(InetAddress.getByName(addr))));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
		temp.remove(sid);
		temp.union(serverView);
		temp.shrink();

		//Check for health of the servers and remove the non-reachable servers from the temp/current view of the server
		int size = temp.getView().size();
		for (int i = 0; i < size; ) {
			System.out.println("Before probing server" + i + "value is"
					+ temp.getView().get(i));
			if (!checkHealth(temp.getView().get(i))) {
				System.out
						.println("Removing from temp" + temp.getView().get(i));
				temp.remove(temp.getView().get(i));
				size = temp.getView().size();
			} else {i++;}
		}

		
		for (Server s : temp.getView()) {
			serverView.insert(s);
		}
		
		temp.insert(sid);
		temp.shrink();
	    
		//Delete the non-reachable server IPs and update Simple DB
		for (int i = 0; i < db.getView().size(); i++) {
			boolean check = false;
			for (Server s : temp.getView()) {
				if (db.getView().get(i).toString().contains(s.toString())) {
					check = true;
				}

			}
			if (check == false) {
				sdb.deleteAttributes(new DeleteAttributesRequest(
						simpleDBDomain, db.getView().get(i).toString()));
			}
		}

     }

	public boolean checkHealth(Server s) {
		
		//Send a RPC call to server (opcode 3 - ping) 
		
		ArrayList<String> ListToSend = new ArrayList<String>();
		RPCClient rpcClient = new RPCClient();
		ListToSend.add(s.toString());
		DatagramPacket rcvPkt = rpcClient.Stub(null, 3, null, null, ListToSend);
		if (rcvPkt != null) {
			System.out.println(s + " Healthy");
			return true;
		} else {
			System.out.println(s + " Unhealthy");
			return false;
		}
	}
    
	
	public List<Server> getServers() {
		
		//Get the current servers view - for printing
		
		@SuppressWarnings("unchecked")
		List<Server> ss = (List<Server>) ((ArrayList<Server>) serverView
				.getView()).clone();
		return ss;
	}

	public String selectServer() {
		
		//Shuffle the list and randomly choose a server(backup)
		
		List<Server> ss = getServers();
		Collections.shuffle(ss);

		if (ss.size() == 0) {
			return null;
		}

		System.out.println("Select server is returning" + ss.get(0).toString());
		return ss.get(0).toString();
	}
}