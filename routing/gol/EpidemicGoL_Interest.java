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
public class EpidemicGoL_Interest implements routing.RoutingDecisionEngine {

    public final static String CONTENT_PROPERTY_INTEREST = "interest";
    public final static String CONTENT_PROPERTY_COUNTER = "counter";
    public final static String THERSHOLD_COPY = "copy";
    public final static String THERSHOLD_DROP = "drop";

//    protected int counter;
    protected int thresholdCopy, thresholdDrop;
    private List<String> tempPeerId;
    private Set<String> tombstones;

    public EpidemicGoL_Interest(Settings s) {
        thresholdCopy = s.getInt(THERSHOLD_COPY);
        thresholdDrop = s.getInt(THERSHOLD_DROP);
        tempPeerId = new ArrayList<>();
        tombstones = new HashSet<>();
    }

    public EpidemicGoL_Interest(EpidemicGoL_Interest prototype) {
        this.thresholdCopy = prototype.thresholdCopy;
        this.thresholdDrop = prototype.thresholdDrop;
        tempPeerId = new ArrayList<>();
        tombstones = new HashSet<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer, String interest) {
//        System.out.println("up up up up up up up up");
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer, String interest) {
        this.tempPeerId.clear();
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer, String interest) {
        for (Message m : peer.getMessageCollection()) {
            this.tempPeerId.add(m.getId());
        }
    }

    @Override
    public boolean newMessage(Message m, String interest) {
        m.addProperty(CONTENT_PROPERTY_INTEREST, interest);
        m.addProperty(CONTENT_PROPERTY_COUNTER, 0);
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost, String interest) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost, String interest) {
        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, String interest) {
        String messageInterest = (String) m.getProperty(CONTENT_PROPERTY_INTEREST);
        Integer messageCounter = (Integer) m.getProperty(CONTENT_PROPERTY_COUNTER);
        if (this.tombstones.contains(m.getId())) {
            return false;
        }
        if (messageInterest.equals(otherHost.getRouter().getInterest())) {
            return true;
        } else if (!messageInterest.equals(otherHost.getRouter().getInterest())) {
            for (String msgId : tempPeerId) {
                if (m.getId().equals(msgId)) {
                    messageCounter--;
                    System.out.println("-");
                } else {
                    messageCounter++;
                    System.out.println("+");
                }
                m.updateProperty(CONTENT_PROPERTY_COUNTER, messageCounter);
                Integer updCounter = (Integer) m.getProperty(CONTENT_PROPERTY_COUNTER);
                if (updCounter == thresholdDrop) {
                    this.tombstones.add(m.getId());
                }

//            System.out.println("Message counter " + m.getId() + " = " + updCounter);
                if (updCounter == thresholdCopy) {
                    m.updateProperty(CONTENT_PROPERTY_COUNTER, 0);
                    return true;
                }
                System.out.println("MESSAGE ID : " + m.getId() + "\t\t\tCOUNTER VALUE : " + m.getProperty(CONTENT_PROPERTY_COUNTER)
                        + "\t\t\t INTEREST : " + m.getProperty(CONTENT_PROPERTY_INTEREST));
            }
        }
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost, String interest
    ) {
        return true;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld, String interest
    ) {
        if (this.tombstones.contains(m.getId())) {
            return true;
        }
        return false;
    }

    private EpidemicGoL_Interest getOtherDecisionEngine(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";
        return (EpidemicGoL_Interest) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new EpidemicGoL_Interest(this);
    }

    @Override
    public void update(DTNHost thisHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

//    @Override
//    public void update(DTNHost hostKu
//    ) {
////        Collection<Message> messageCollection = hostKu.getMessageCollection();
//        Vector<String> messagesToDelete = new Vector<String>();
//        DecisionEngineRouter router = (DecisionEngineRouter) hostKu.getRouter();
//
//        for (Message message : hostKu.getMessageCollection()) {
//            Integer messageCounter = (Integer) message.getProperty(CONTENT_PROPERTY_COUNTER);
//            if (messageCounter == thresholdDrop) {
//                messagesToDelete.add(message.getId());
//                hostKu.getRouter().deleteMessage(message.getId(), true);
//                //   m.updateProperty
//            }
//        }
//        for (String messageID : messagesToDelete) {
//            router.deleteMessage(messageID, true);
//            System.out.println(messageID + " deleted");
//        }
//    }
//}
//
//}
