/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.gol;

import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author SuryoDeboAyas
 */
public class EpidemicGameOfLife implements RoutingDecisionEngine {

    public final static String CONTENT_PROPERTY_COUNTER = "counter";
    public final static String THERSHOLD_COPY = "thCopy";
    public final static String THERSHOLD_DELETE = "thDelete";

//    protected int counter;
    private final int thresholdCopy, thresholdDelete;
    protected Set<String> tombstone;

    public EpidemicGameOfLife(Settings s) {
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
        tombstone = new HashSet<>();
    }

    public EpidemicGameOfLife(EpidemicGameOfLife prototype) {
        this.thresholdCopy = prototype.thresholdCopy;
        this.thresholdDelete = prototype.thresholdDelete;
        tombstone = new HashSet<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer, String interest) {
        List<String> peerList = new ArrayList<>();
        EpidemicGameOfLife p = getOtherDecisionEngineRouter(peer);
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
            if (p.tombstone.contains(myMsg.getId())) {
                myMsg.updateProperty(CONTENT_PROPERTY_COUNTER, (int) myMsg.getProperty(CONTENT_PROPERTY_COUNTER) - 1);
            }
//            System.out.println("BEFORE MESSAGE ID : " + myMsg.getId() + "\t\t\tCOUNTER VALUE : " + myMsg.getProperty(CONTENT_PROPERTY_COUNTER));
            Integer updateCounter = (Integer) myMsg.getProperty(CONTENT_PROPERTY_COUNTER);
            /**
             * Message marked for being deleted
             */
            if (updateCounter <= thresholdDelete) {
                msgToDelete.add(myMsg.getId());
            }
        }
        DecisionEngineRouter thisRouter = (DecisionEngineRouter) thisHost.getRouter();
        /**
         * Message deleted
         */
        for (String msgId : msgToDelete) {
            thisRouter.deleteMessage(msgId, false);
        }
        msgToDelete.clear();
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer, String interest) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer, String interest) {
    }

    @Override
    public boolean newMessage(Message m, String interest) {
        m.addProperty(CONTENT_PROPERTY_COUNTER, 0);
        tombstone.add(m.getId());
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost, String interest) {
        m.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost, String interest) {
        // jika pesan yang akan diterima terdapat pada tombstone, false

        if (this.tombstone.contains(m.getId())) {
            return false;
        }
//        Collection<Message> messageCollection = thisHost.getMessageCollection();
//        for (Message message : messageCollection) {
//            if (m.getId().equals(message.getId())) {
//                return false;
//            }
//        }
        tombstone.add(m.getId());
        return !thisHost.getRouter().hasMessage(m.getId());
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, String interest) {
        Integer msgCounter = (Integer) m.getProperty(CONTENT_PROPERTY_COUNTER);
        EpidemicGameOfLife p = getOtherDecisionEngineRouter(otherHost);
        /**
         * Message counter reseted to 0 when it's about to sent
         */
//        if(m.getTo() == otherHost){
//            m.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
//            return true;
//        }
//        if (p.tombstone.contains(m.getId())) {
//            m.updateProperty(CONTENT_PROPERTY_COUNTER, (int) m.getProperty(CONTENT_PROPERTY_COUNTER) - 1);
//        }
        if (!p.tombstone.contains(m.getId())) {
            if (msgCounter >= thresholdCopy) {
                m.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
                return true;
            }
        }
//        System.out.println("AFTER MESSAGE ID : " + m.getId() + "\t\t\tCOUNTER VALUE : " + m.getProperty(CONTENT_PROPERTY_COUNTER));
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost, String interest) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld, String interest) {
        return false;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new EpidemicGameOfLife(this);
    }

    private EpidemicGameOfLife getOtherDecisionEngineRouter(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";
        return (EpidemicGameOfLife) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost thisHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
