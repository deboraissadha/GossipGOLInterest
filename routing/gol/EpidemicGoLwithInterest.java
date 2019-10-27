/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.gol;

import core.*;
import java.util.*;
import routing.RoutingDecisionEngine;

/**
 *
 * @author SuryoDeboAyas
 */
public class EpidemicGoLwithInterest implements RoutingDecisionEngine {

    public final static String CONTENT_PROPERTY_INTEREST = "interest";
    public final static String CONTENT_PROPERTY_COUNTER = "counter";
    public final static String THERSHOLD_COPY = "thCopy";
    public final static String THERSHOLD_DELETE = "thDelete";

//    protected int counter;
    private final int thresholdCopy, thresholdDelete;

    public EpidemicGoLwithInterest(Settings s) {
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

    public EpidemicGoLwithInterest(EpidemicGoLwithInterest prototype) {
        this.thresholdCopy = prototype.thresholdCopy;
        this.thresholdDelete = prototype.thresholdDelete;
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer, String interest) {
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
    public void connectionDown(DTNHost thisHost, DTNHost peer, String interest) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer, String interest) {
    }

    @Override
    public boolean newMessage(Message m, String interest) {
        m.addProperty(CONTENT_PROPERTY_INTEREST, interest);
        m.addProperty(CONTENT_PROPERTY_COUNTER, new Integer(0));
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost, String interest) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost, String interest) {
//        for (Message msg : thisHost.getMessageCollection()) {
//            if (msg.getId().equals(m.getId())) {
//                return false;
//            }
//        }
//        return true;
        return !thisHost.getRouter().hasMessage(m.getId());
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, String interest) {
        String msgInterest = (String) m.getProperty(CONTENT_PROPERTY_INTEREST);
        Integer msgCounter = (Integer) m.getProperty(CONTENT_PROPERTY_COUNTER);
        /**
         * If otherHost is the message destination
         */
//        if (m.getTo() == otherHost) {
//            m.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
//            return true;
//        }

        /**
         * Message counter reseted to 0 when it's about to sent
         */
        if (msgInterest.equals(otherHost.getRouter().getInterest()) ) {
            m.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
            return true;
        }
        if (msgCounter == thresholdCopy) {
            m.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
            return true;
        }

//        System.out.println("MESSAGE ID : " + m.getId() + "\t\t\tCOUNTER VALUE : " + m.getProperty(CONTENT_PROPERTY_COUNTER)
//                + "\t\t\t INTEREST : " + m.getProperty(CONTENT_PROPERTY_INTEREST));

        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost, String interest
    ) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld, String interest
    ) {
        return false;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new EpidemicGoLwithInterest(this);
    }

    @Override
    public void update(DTNHost thisHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
