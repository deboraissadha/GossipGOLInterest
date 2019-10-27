/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.interest;

import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author Debora Issadha Universitas Sanata Dharma
 * Fungsi kelas : Router Gossip Game of Life dengan Interest
 */
public class GossipGoLwithInterestRouter implements RoutingDecisionEngine, Tombstone {

    public final static String CONTENT_PROPERTY_INTEREST = "interest";
    public final static String CONTENT_PROPERTY_COUNTER = "counter";
    public final static String THERSHOLD_COPY = "thCopy";
    public final static String THERSHOLD_DELETE = "thDelete";

    private final int thresholdCopy, thresholdDelete;
    protected Set<String> tombstone;

    public GossipGoLwithInterestRouter(Settings s) {
        if (s.contains(THERSHOLD_COPY)) {
            this.thresholdCopy = s.getInt(THERSHOLD_COPY);
        } else {
            this.thresholdCopy = 3; // default threshold Copy
        }
        if (s.contains(THERSHOLD_DELETE)) {
            this.thresholdDelete = s.getInt(THERSHOLD_DELETE);
        } else {
            this.thresholdDelete = -3; // default threshold Delete
        }
        tombstone = new HashSet<>();
    }

    public GossipGoLwithInterestRouter(GossipGoLwithInterestRouter prototype) {
        this.thresholdCopy = prototype.thresholdCopy;
        this.thresholdDelete = prototype.thresholdDelete;
        tombstone = new HashSet<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer, String interest) {
        List<String> peerList = new ArrayList<>();
        GossipGoLwithInterestRouter p = getOtherDecisionEngineRouter(peer);
        Vector<String> msgToDelete = new Vector<>();

        /**
         * Checking for each message in thisHost collection if thisHost has the
         * same message with peer's message then the message counter decremented
         * else incremented
         */
        //REVISED LANGSUNG CEK TB GAK CEK PEERLIST
        for (Message myMsg : thisHost.getMessageCollection()) {
            Integer msgCounter = (Integer) myMsg.getProperty(CONTENT_PROPERTY_COUNTER);
            String msgInterest = (String) myMsg.getProperty(CONTENT_PROPERTY_INTEREST);

            //cek interest dulu baru hitung counter
            if (msgInterest.equals(peer.getRouter().getInterest())) {
                if (!peer.getRouter().hasMessage(myMsg.getId()) || !p.tombstone.contains(myMsg.getId())) {
                    myMsg.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
                    this.shouldSendMessageToHost(myMsg, peer, interest);
                }
            } else {
                if (peer.getRouter().hasMessage(myMsg.getId()) || p.tombstone.contains(myMsg.getId())) {
                    msgCounter--;
                } else {
                    msgCounter++;
                }
            }
            myMsg.updateProperty(CONTENT_PROPERTY_COUNTER, msgCounter);

//            System.out.println("BEFORE MESSAGE ID : " + myMsg.getId() + "\t\t\tCOUNTER VALUE : " + myMsg.getProperty(CONTENT_PROPERTY_COUNTER)
//                    + "\t\t\t INTEREST : " + myMsg.getProperty(CONTENT_PROPERTY_INTEREST));
            Integer updateCounter = (Integer) myMsg.getProperty(CONTENT_PROPERTY_COUNTER);
            /**
             * Message marked for being deleted
             */
            if (updateCounter == thresholdDelete) {
                msgToDelete.add(myMsg.getId());
                tombstone.add(myMsg.getId());
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
        m.addProperty(CONTENT_PROPERTY_INTEREST, interest);
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
        //gagal kirim, jika pesan m terdapat pada tombstone thisHost
        if (this.tombstone.contains(m.getId())) {
            return false;
        }
//        tombstone.add(m.getId());
        // kirim pesan hanya jika tidak terdapat pesan m pada message Collection thisHost
        return !thisHost.getRouter().hasMessage(m.getId());
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, String interest) {
        Integer msgCounter = (Integer) m.getProperty(CONTENT_PROPERTY_COUNTER);
        String msgInterest = (String) m.getProperty(CONTENT_PROPERTY_INTEREST);
        GossipGoLwithInterestRouter p = getOtherDecisionEngineRouter(otherHost);
        DecisionEngineRouter peer = (DecisionEngineRouter) (otherHost.getRouter());
        /**
         * Message counter reseted to 0 when it's about to send
         */
        if (!otherHost.getRouter().hasMessage(m.getId()) && !p.tombstone.contains(m.getId())) {
            // jika interest sama, kirim
            if (m.getProperty(CONTENT_PROPERTY_INTEREST).equals(peer.getInterest())) {
                m.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
                return true;
                // jika beda, cek threshold
            } else if (msgCounter == thresholdCopy) {
                m.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
                return true;
            }
//            System.out.println("AFTER MESSAGE ID : " + m.getId() + "\t\t\tCOUNTER VALUE : " + m.getProperty(CONTENT_PROPERTY_COUNTER)
//                    + "\t\t\t INTEREST : " + m.getProperty(CONTENT_PROPERTY_INTEREST));
        }
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost, String interest) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld, String interest) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new GossipGoLwithInterestRouter(this);
    }

    private GossipGoLwithInterestRouter getOtherDecisionEngineRouter(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";
        return (GossipGoLwithInterestRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public Set<String> getTombstone() {
        return tombstone;
    }

    @Override
    public void update(DTNHost thisHost) {

        for (Message message : thisHost.getMessageCollection()) {
            if (message.getTtl() <= 0) {
                tombstone.add(message.getId());
            }
        }
    }

}
