package cs5300proj1b;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class StateManagement
 */
@WebServlet("")
public class StateManagement extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Cookie[] cookies;
	Timer timer;
	NewGroupMembership gm = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public StateManagement() {
		super();
		timer = new Timer();
		// Schedule the cleanup thread
		timer.scheduleAtFixedRate(new SessionCleanup(), 0, 1 * 60 * 1000);

		// Start the RPC thread
		RPCServer rpc = new RPCServer();
		new Thread(rpc).start();

		Server localServer = null;
		try {
			localServer = new Server(InetAddress.getLocalHost());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Start GroupMembership thread
		try {
			gm = new NewGroupMembership(localServer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(gm).start();
	}

	public void init(ServletConfig config) {
		try {
			super.init(config);
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unused")
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated constructor stub
		/*
		 * doGet method: We enter this method for the first time the servlet is
		 * called.
		 */

		boolean cookiePresent = false;
		String cookieValue;
		String ipLocal = InetAddress.getLocalHost().getHostAddress();
		DatagramPacket rcvPkt = null;
		RPCClient rpcClient = new RPCClient();
		Integer sessionNumber;
		ArrayList<String> ListToSend = new ArrayList<String>();
		String ipBackup = null;

		cookies = request.getCookies();
		String button = request.getParameter("buttonName");
		String replace = request.getParameter("replacetext");

		// Check if there is a cookie(CS5300PROJ1SESSION) already present
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("CS5300PROJ1SESSION".equals(cookie.getName())) {
					cookiePresent = true;
					break;
				}
			}
		}

		/*
		 * Enter for the first time and enter once we press logout. Create
		 * cookie and enter the session values to the Hashmap(primary +backup)
		 */

		if (cookies == null || cookiePresent == false
				&& (button == null && replace == null)) {
			// Session table details
			sessionNumber = Session.sessionNumber;
			Session session = new Session(String.valueOf(sessionNumber) + "$"
					+ ipLocal);
			String[] Array = { session.getMessage(), session.getVersion(),
					String.valueOf(session.getExpirationTime()) };
			System.out.println("Expiration time"
					+ String.valueOf(session.getExpirationTime()));

			// Pick one server at random from group member view
			ipBackup = gm.selectServer();
			session.setlocationMetadata(ipLocal + "#" + ipBackup);

			System.out.println("IPBackup in new session is" + ipBackup);
			ListToSend.add(ipBackup);

			// Cookie details
			cookieValue = session.getSID() + "_" + session.getVersion() + "_"
					+ session.getlocationMetadata();

			// Cookie properties
			Cookie newCookie = new Cookie("CS5300PROJ1SESSION", cookieValue);
			newCookie.setMaxAge(Session.minutes * 60); // Store cookie for 1
														// minute
			response.addCookie(newCookie);

			// Add the session details to hashmap
			Session.session_data.put(session.getSID(), Array);

			// Send a RPC session write to the selected backup server
			rcvPkt = rpcClient.Stub(session.getSID(), 2, session,
					session.getVersion(), ListToSend);
			if (rcvPkt == null) {
				System.out.println("Dint contact backup server for new write ");
				ipBackup = gm.selectServer();

				System.out.println("IPBackup in new session is" + ipBackup);
				ListToSend.clear();
				ListToSend.add(ipBackup);
				// Retry once again to choose another backup server - Last try
				rcvPkt = rpcClient.Stub(session.getSID(), 2, session,
						session.getVersion(), ListToSend);
				if (rcvPkt == null) {
					ipBackup = null; // Null server ID - zero resilience for the
										// session
					session.setlocationMetadata(ipLocal + "#" + ipBackup);
				}
			}

			session.setlocationMetadata(ipLocal + "#" + ipBackup);

			Session.sessionNumber = Session.sessionNumber + 1;

			String serverview = "";
			for (Server s : gm.getServers()) {
				serverview += s.toString() + " $ ";
			}
			System.out.println("Server vew is" + serverview);

			request.setAttribute("SID", session.getSID());
			request.setAttribute("version", session.getVersion());
			request.setAttribute("message", session.getMessage());
			request.setAttribute("expire", session.getExpirationTime());
			request.setAttribute("discard", session.getExpirationTime() + 1000);
			request.setAttribute("locationmetadata",
					session.getlocationMetadata());
			request.setAttribute("svrresponse", "New session write");
			request.setAttribute("serverview", serverview);
			RequestDispatcher view = request.getRequestDispatcher("index.jsp");
			view.forward(request, response);
			return;
		}

		/*
		 * Enter here if there is a cookie present for replace/refresh button
		 * clicked , choosing the local and backup server. In case of refresh -
		 * get the session table values and update the version number and
		 * timestamp In case of replace - get the values and update the version
		 * number and session state(message) along with timestamp Update the
		 * cookie's age as well.
		 */

		else if (cookies != null && cookiePresent == true) {

			for (Cookie cookie : cookies) {
				if ("CS5300PROJ1SESSION".equals(cookie.getName())) {

					cookieValue = cookie.getValue();
					String[] strArr = cookieValue.split("_");
					String SID = strArr[0];
					String[] Array = new String[3];
					String svrResponse = SID;
					Integer vno = Integer.parseInt(strArr[1]);
					Session session = null;

					Array = Session.session_data.get(SID);
					String[] locationMetadata = strArr[2].split("#");

					for (String loc : locationMetadata) {
						if (loc != null) {
							ListToSend.add(loc);
						}
					}

					// If the data is not present in the local Hash Table or
					// version number mismatch(stale data in the local)
					// Session Read from the local/backup Server in the cookie
					// If the current primary/backup do not respond then erase
					// the cookie
					if (Array == null
							|| (Array != null && Integer.parseInt(Array[1]) < Integer
									.parseInt(strArr[1]))) {
						System.out.println("Read to cookie metadata servers");
						rcvPkt = rpcClient.Stub(SID, 1, null, strArr[1],
								ListToSend);
						if (rcvPkt == null) {
							System.out
									.println("Both servers dint respond, erasing the cookie");
							eraseCookie(request, response, 2);

						} else {

							byte[] rcvList = rcvPkt.getData();
							ByteArrayInputStream bis = new ByteArrayInputStream(
									rcvList);
							ObjectInput in = new ObjectInputStream(bis);
							String output = null;
							try {
								output = (String) in.readObject();
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							String[] rcvdata = output.split("@");
							if ((rcvdata.length < 2)) {
								System.out
										.println("Both servers dint respond, erasing the cookie");
								eraseCookie(request, response, 2);
							} else {
								System.out.println("Read from server success");

								session = new Session(SID);
								session.setMessage(rcvdata[1]);
								session.setVersion(rcvdata[2]);
								session.setExpirationTime(Long
										.parseLong(rcvdata[3]));
								svrResponse = rcvPkt.getAddress().toString();
							
							}
						}
					}

					if (Array != null) {
						session = new Session(SID);
					}

					// Replace
					if (button != null && button.contains("button 1")
							&& replace != null) {
						Array[0] = replace;
						session.setMessage(replace);
					}

					// Refresh or Replace
					if (Array != null) {
						session.setMessage(Array[0]);
					}
					session.setExpirationTime((new Date().getTime() + ((Session.minutes * 60) * 1000))); // Update
																											// timestamp
					Array[2] = String.valueOf(session.getExpirationTime());
					Array[1] = (++vno) + "";
					// Increment version number
					session.setVersion(vno.toString());
					// Update session table
					Session.session_data.put(SID, Array);

					// Get the new server from its view
					ipBackup = gm.selectServer(); // Random Server
					System.out.println("IPBACKUP in refresh/replace is"
							+ ipBackup);

					ListToSend.clear();
					if (ipBackup != null) {
						ListToSend.add(ipBackup);
					}
					// Session write to Backup server
					rcvPkt = rpcClient.Stub(SID, 2, session,
							session.getVersion(), ListToSend);
					if (rcvPkt == null) {
						ipBackup = null;
					}

					session.setlocationMetadata(ipLocal + "#" + ipBackup);

					// Update cookie details
					cookieValue = SID + "_" + vno + "_"
							+ session.getlocationMetadata();
					cookie.setValue(cookieValue);
					cookie.setMaxAge(Session.minutes * 60);
					response.addCookie(cookie);

					String serverview = "";

					for (Server s : gm.getServers()) {
						serverview += s.toString() + " $ ";
					}

					request.setAttribute("SID", session.getSID());
					request.setAttribute("version", session.getVersion());
					request.setAttribute("message", session.getMessage());
					request.setAttribute("expire", session.getExpirationTime());
					request.setAttribute("discard",
							session.getExpirationTime() + 1000);
					request.setAttribute("locationmetadata",
							session.getlocationMetadata());
					request.setAttribute("svrresponse", svrResponse);
					request.setAttribute("serverview", serverview);

					if (!response.isCommitted()) {
						RequestDispatcher view = request
								.getRequestDispatcher("index.jsp");
						view.forward(request, response);
					}
				}
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/*
		 * doPost method: We would enter here once submit button is pressed from
		 * the index.jsp form If the button is replace/refresh, call doGet
		 * method If the button is logout -> call the erase cookie method
		 */
		String button = request.getParameter("buttonName");
		if (button.contains("button 1") || button.contains("button 2")) {
			doGet(request, response);
		} else {
			eraseCookie(request, response, 1);
		}
	}

	private void eraseCookie(HttpServletRequest req, HttpServletResponse resp,
			int oper) throws ServletException, IOException {
		/*
		 * eraseCookie method Called when logout button is clicked Clears the
		 * cookie and session table entry
		 */
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("CS5300PROJ1SESSION".equals(cookie.getName())) {
					String cookieValue = cookie.getValue();
					String[] strArr = cookieValue.split("_");
					String SID = strArr[0];
					Session session = new Session(SID);
					ArrayList<String> ListToSend = new ArrayList<String>();
					String[] locationMetadata = strArr[2].split("#");
					System.out
							.println("In erase cookie, clearing cookie values");

					// Remove from session table
					Session.session_data.remove(SID);

					// Invalidate the cookie
					cookie.setMaxAge(0);
					resp.addCookie(cookie);

					if (!resp.isCommitted()) {
						if (oper == 1) {
							RequestDispatcher view = req
									.getRequestDispatcher("/logout.jsp");
							view.forward(req, resp);
						} else {
							resp.sendRedirect(req.getContextPath()
									+ "/Error.jsp");
						}

					}
				}
			}
		}
	}
}
