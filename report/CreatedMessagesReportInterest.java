/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import java.util.List;

/**
 * Reports information about all created messages. Messages created during
 * the warm up period are ignored.
 * For output syntax, see {@link #HEADER}.
 */
public class CreatedMessagesReportInterest extends Report implements MessageListener {
	public static String HEADER = "# time\t\t ID\tsize\tInterest\tfromHost\ttoHost\tTTL  " + 
		"isResponse";

	/**
	 * Constructor.
	 */
	public CreatedMessagesReportInterest() {
		init();
	}
	
	@Override
	public void init() {
		super.init();
		write(HEADER);
	}
        
         private String getPathString(Message m) {
        List<DTNHost> hops = m.getHops();
        String str = m.getFrom().toString();

        for (int i=1; i<hops.size(); i++) {
            str += "->" + hops.get(i);
        }

        return str;
    }


        @Override
	public void newMessage(Message m) {
		if (isWarmup()) {
			return;
		}
		
		int ttl = m.getTtl();
//                System.out.println(format(getSimTime()) + " \t " + m.getId() + " \t " + 
//				m.getSize() + " \t " + m.getProperty(m.getContentInterestM()) + " \t " + m.getFrom() + " \t " + m.getTo() + " \t " +
//				(ttl != Integer.MAX_VALUE ? ttl : "n/a") +  
//				(m.isResponse() ? " Y " : " N "));
		write(format(getSimTime()) + " \t " + m.getId() + " \t " + 
				m.getSize() + " \t " + m.getProperty("interest") + " \t\t " + m.getFrom() + " \t\t " + m.getTo() + " \t " +
				(ttl != Integer.MAX_VALUE ? ttl : "n/a") +  
				(m.isResponse() ? " Y " : " N "));
	}
	
	// nothing to implement for the rest
	public void messageTransferred(Message m, DTNHost f, DTNHost t,boolean b) {}
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {}
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}

	@Override
	public void done() {
		super.done();
	}
}
