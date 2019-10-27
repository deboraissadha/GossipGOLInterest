/* 
 * Debora Issadha
 */
package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.*;

public class TotalMessageRelayedperTimeperInterest extends Report implements MessageListener, UpdateListener {

    public final static String CONTENT_PROPERTY_INTEREST = "interest";
    public static final String INTERVAL_COUNT = "interval";
    public static final int DEFAULT_INTERVAL_COUNT = 3600;
    private int interval;

    private double lastRecord = Double.MIN_VALUE;

    private Map<String, Integer> relayedInterest;
    private Map<Double, Map<String, Integer>> relayedInterestperTime;

    public TotalMessageRelayedperTimeperInterest() {
        Settings s = getSettings();
        if (s.contains(INTERVAL_COUNT)) {
            interval = s.getInt(INTERVAL_COUNT);
        } else {
            interval = -1;
        }
        if (interval < 0) {
            interval = DEFAULT_INTERVAL_COUNT;
        }
        init();
    }

    @Override
    protected void init() {
        super.init();
        this.interval = interval;
        this.relayedInterest = new HashMap<>();
        this.relayedInterestperTime = new HashMap<>();
    }

    @Override
    public void newMessage(Message m) {
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        String interest = (String) m.getProperty(CONTENT_PROPERTY_INTEREST);
        Integer iRelayed = getNrofRelayedMessage(interest);
        relayedInterest.put(interest, iRelayed + 1);
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (getSimTime() - lastRecord >= interval) {
            this.lastRecord = getSimTime() - getSimTime() % interval;
            relayedInterestperTime.put(lastRecord, relayedInterest);
            String statsText = lastRecord + "";
            for (Map.Entry<String, Integer> e : relayedInterest.entrySet()) {
                String key = e.getKey();
                Integer value = e.getValue();                
                statsText += "\t" + key + "\t" + value + "\n";
            }
            write(statsText);
        }
    }

    private int getNrofRelayedMessage(String interest) {
        if (relayedInterest.containsKey(interest)) {
            return relayedInterest.get(interest);
        } else {
            return 0;
        }
    }
}
