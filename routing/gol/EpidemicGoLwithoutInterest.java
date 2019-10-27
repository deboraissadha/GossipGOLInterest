/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.gol;

import core.*;
import java.util.*;
import routing.*;

/**
 *
 * @author SuryoDeboAyas
 */
public class EpidemicGoLwithoutInterest implements RoutingDecisionEngine_Improved {

    public final static String CONTENT_PROPERTY_COUNTER = "counter";
    public final static String THERSHOLD_COPY = "thCopy";
    public final static String THERSHOLD_DELETE = "thDelete";

//    protected int counter;
    private final int thresholdCopy, thresholdDelete;

    public EpidemicGoLwithoutInterest(Settings s) {
        if (s.contains(THERSHOLD_COPY)) {
            this.thresholdCopy = s.getInt(THERSHOLD_COPY);
        } else {
            this.thresholdCopy = 3; // default thCopy
        }
        if (s.contains(THERSHOLD_DELETE)) {
            this.thresholdDelete = s.getInt(THERSHOLD_DELETE);
        } else {
            this.thresholdDelete = -3; // default thDelete
        }
    }

    public EpidemicGoLwithoutInterest(EpidemicGoLwithoutInterest prototype) {
        this.thresholdCopy = prototype.thresholdCopy;
        this.thresholdDelete = prototype.thresholdDelete;
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        List<String> peerList = new ArrayList<>();
        Vector<String> msgToDelete = new Vector<>();

        /**
         * Put peer message's id to the list
         */
        for (Message peerMsg : peer.getMessageCollection()) {
            peerList.add(peerMsg.getId());
        }
        /**
         * Checking for each message in thisHost collection if thisHost has the
         * same message with peer's message then the message counter decremented
         * else incremented
         */

        for (Message myMsg : thisHost.getMessageCollection()) {
            Integer msgCounter = (Integer) myMsg.getProperty(CONTENT_PROPERTY_COUNTER);

            if (peerList.contains(myMsg.getId())) {
                msgCounter--;
            } else {
                msgCounter++;
            }
            myMsg.updateProperty(CONTENT_PROPERTY_COUNTER, msgCounter);
            Integer updateCounter = (Integer) myMsg.getProperty(CONTENT_PROPERTY_COUNTER);
            /**
             * Message marked for being deleted
             */
            if (updateCounter == thresholdDelete) {
                msgToDelete.add(myMsg.getId());
                myMsg.updateProperty(CONTENT_PROPERTY_COUNTER, updateCounter);
            }
        }
        /**
         * Message deleted
         */
        for (String msgId : msgToDelete) {
            thisHost.deleteMessage(msgId, true);
        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        m.addProperty(CONTENT_PROPERTY_COUNTER, new Integer(0));
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return !thisHost.getRouter().hasMessage(m.getId());
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        Integer msgCounter = (Integer) m.getProperty(CONTENT_PROPERTY_COUNTER);
        /**
         * Message counter reseted to 0 when it's about to sent
         */
        if (msgCounter == thresholdCopy) {
            m.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
            return true;
        }

       // System.out.println("MESSAGE ID : " + m.getId() + "\t\t\tCOUNTER VALUE : " + m.getProperty(CONTENT_PROPERTY_COUNTER)
         //       + "\t\t\t INTEREST : " + m.getProperty(CONTENT_PROPERTY_INTEREST));

        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost
    ) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld
    ) {
        return false;
    }

    @Override
    public RoutingDecisionEngine_Improved replicate() {
        return new EpidemicGoLwithoutInterest(this);
    }

}
