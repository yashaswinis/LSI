package cs5300proj1b;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {
   public InetAddress ip;
   
   /**
    * Dummy constructor
    */
   public Server() {
      try {
         ip = InetAddress.getByName("127.0.0.1");
      } catch (UnknownHostException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   public Server(InetAddress sip) {
     ip = sip;
   }
  
   public String toString() {
      return ip.getHostAddress();
   }
 }
