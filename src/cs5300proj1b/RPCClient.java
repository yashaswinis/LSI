package cs5300proj1b;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

public class RPCClient {
	public static Integer destPort = 5300;
	public static Integer maxPacketSize = 512;
	public static Integer wait_time_seconds = 10;

	public DatagramPacket Stub(String sessionID, Integer opCode,
			Session session, String versionNumber, ArrayList<String> ListToSend) {

		DatagramSocket rpcSocket = null;
		String callID = null;
		String callIDR = null;
		byte[] inBuf;
		DatagramPacket recvPkt = null;

		switch (opCode) {

		case 1:

			// Session Read
			if (ListToSend.size() > 0) {
				try {
					rpcSocket = new DatagramSocket();
					callID = UUID.randomUUID().toString();

					byte[] outBuf;
					
					//Marshall the callID + opcode +sessionID + version Number to send the server
					
					String List = callID + "@" + opCode + "@" + sessionID + "@"
							+ versionNumber;
					outBuf = List.getBytes();

					//Send the Packet to all the IPs listed by the client
					for (String destAddr : ListToSend) {
						InetAddress destAddress = InetAddress
								.getByName(destAddr);
						System.out.println("destAddr" + destAddr
								+ "destAddress" + destPort);
						DatagramPacket sendPkt = new DatagramPacket(outBuf,
								outBuf.length, destAddress, destPort);
						
						rpcSocket.send(sendPkt);
						System.out.println("PacketSent");
					}

					// Set timeout
					rpcSocket.setSoTimeout(wait_time_seconds * 1000);

				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				inBuf = new byte[maxPacketSize];
				recvPkt = new DatagramPacket(inBuf, inBuf.length);
				
				try {
					do {
						recvPkt.setLength(inBuf.length);
						System.out.println("Waiting for reply");
						rpcSocket.receive(recvPkt);
						System.out.println("Got Reply1");
						String[] RecvList = new String(recvPkt.getData(), 0,
								recvPkt.getLength()).split("@");
						callIDR = RecvList[0];
					} while (callIDR.contentEquals(callID)); //Check for callID
				} catch (InterruptedIOException iioe) {
					// timeout
					recvPkt = null;
				} catch (IOException ioe) {
					
				}
				rpcSocket.close();
			}
			return recvPkt;
			
		case 2:
			if (ListToSend.size() > 0) {
				    // Session Write
				try {
					rpcSocket = new DatagramSocket();
					callID = UUID.randomUUID().toString();

					byte[] outBuf;
					String List = callID + "@" + opCode + "@" + sessionID + "@"
							+ session.getVersion() + "@" + session.getMessage()
							+ "@" + session.getExpirationTime();
					outBuf = List.getBytes();

					//Send to the list of servers added by the client
					for (String destAddr : ListToSend) {
						InetAddress destAddress = InetAddress
								.getByName(destAddr);
						System.out.println("destAddr" + destAddr
								+ "destAddress" + destPort);
						DatagramPacket sendPkt = new DatagramPacket(outBuf,
								outBuf.length, destAddress, destPort);
						
						rpcSocket.send(sendPkt);
					}

					// Set timeout
					rpcSocket.setSoTimeout(wait_time_seconds * 1000);
					System.out.println("PacketSent");
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				inBuf = new byte[maxPacketSize];
				recvPkt = new DatagramPacket(inBuf, inBuf.length);
				try {
					do {
						recvPkt.setLength(inBuf.length);
						System.out.println("Waiting for reply");
						rpcSocket.receive(recvPkt);
						System.out.println("Got Reply2");
						String[] RecvList = new String(recvPkt.getData(), 0,
								recvPkt.getLength()).split("@");

						callIDR = RecvList[0];
					} while (callIDR.contentEquals(callID)); //Check for CallID

				} catch (InterruptedIOException iioe) {
					// timeout
					recvPkt = null;
					
				} catch (IOException ioe) {
					// other error
				}
				rpcSocket.close();
			}
			return recvPkt;

		case 3:
			// Probe - send only the callid and opcode to server -Ping
			try {
				rpcSocket = new DatagramSocket();
				callID = UUID.randomUUID().toString();

				byte[] outBuf;
				String List = callID + "@" + opCode;
				outBuf = List.getBytes();

				for (String destAddr : ListToSend) {
					InetAddress destAddress = InetAddress.getByName(destAddr);
					System.out.println("destAddr" + destAddr + "destAddress"
							+ destPort);
					DatagramPacket sendPkt = new DatagramPacket(outBuf,
							outBuf.length, destAddress, destPort);
					rpcSocket.send(sendPkt);
					System.out.println("PacketSent3");
				}

				// Set timeout
				rpcSocket.setSoTimeout(wait_time_seconds * 1000);

			} catch (SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			inBuf = new byte[maxPacketSize];
			recvPkt = new DatagramPacket(inBuf, inBuf.length);
			try {
				do {
					recvPkt.setLength(inBuf.length);
					rpcSocket.receive(recvPkt);
					System.out.println("Got Reply3");
					String[] RecvList = new String(recvPkt.getData(), 0,
							recvPkt.getLength()).split("@");
					callIDR = RecvList[0];
				} while (callIDR.contentEquals(callID));
			} catch (InterruptedIOException iioe) {
				// timeout
				recvPkt = null;
				
			} catch (IOException ioe) {
				// other error
			}
			rpcSocket.close();
			return recvPkt;
		}

		return recvPkt;
	}
}
