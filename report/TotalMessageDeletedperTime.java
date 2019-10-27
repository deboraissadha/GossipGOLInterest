/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.util.*;
import core.*;

public class TotalMessageDeletedperTime extends Report implements MessageListener, UpdateListener {

    public final static String CONTENT_PROPERTY_COUNTER = "counter";
    public static final String INTERVAL_COUNT = "interval";
    public static final int DEFAULT_INTERVAL_COUNT = 3600;
    public static final String ENGINE_SETTING = "decisionEngine";
    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    private Map<Double, Integer> nrofDel;
    private int nrofDeleted;

    /**
     * Constructor.
     */
    public TotalMessageDeletedperTime() {
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
        this.nrofDel = new HashMap<>();
        this.nrofDeleted = 0;
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (!dropped) {
            this.nrofDeleted++;
        }
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {

    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {

    }

    @Override
    public void newMessage(Message m) {

    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void done() {
        String statsText = "TIME\tDELETED\n";
        for (Map.Entry<Double, Integer> entry : nrofDel.entrySet()) {
            double key = entry.getKey();
            Integer value = entry.getValue();
            statsText += key + "\t" + value + "\n";
        }
        write(statsText);
        super.done();
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (getSimTime() - lastRecord >= interval) {
            this.lastRecord = getSimTime() - getSimTime() % interval;
            nrofDel.put(lastRecord, nrofDeleted);
        }
    }
}
