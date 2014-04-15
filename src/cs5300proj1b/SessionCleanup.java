package cs5300proj1b;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;

class SessionCleanup extends TimerTask {
	public void run() {
		/**
		 * CleanUp method: Iterates through the hash map, deleting the entries
		 * for which we have reached the expiry time
		 */
		Iterator it = Session.session_data.entrySet().iterator();
		synchronized (it) { // synchronized keyword avoids concurrent thread access
			while (it.hasNext()) {

				Map.Entry<String, String[]> pairs = (Map.Entry<String, String[]>) it.next();
				String Array[] = pairs.getValue();

				long expiration = Long.parseLong(Array[2]) + 1000; //Discard time
				long now = new Date().getTime();
				System.out.println("Expiration time" + expiration + "now is"+ now);
				if (expiration < now) { //Remove the value if its past the discardtime
					System.out.println("Came here removed the value");
					it.remove();
				}
			}
		}
	}
}