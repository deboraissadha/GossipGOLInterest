package routing.interest;

import core.*;
import java.util.HashSet;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 * @author Debora Issadha Sanata Dharma University
 * Fungsi kelas : Router Epidemic dengan Interest
 */
public class EpidemicWithInterest implements routing.RoutingDecisionEngine, Tombstone {

    public final static String CONTENT_PROPERTY_INTEREST = "interest";
    protected Set<String> tombstone;

    public EpidemicWithInterest(Settings s) {
        tombstone = new HashSet<>();
    }

    public EpidemicWithInterest(EpidemicWithInterest prototype) {
        tombstone = new HashSet<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer, String interest) {
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
        tombstone.add(m.getId());
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost, String interest) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost, String interest) {
        tombstone.add(m.getId());
        return !thisHost.getRouter().hasMessage(m.getId());
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, String interest) {
        DecisionEngineRouter peer = (DecisionEngineRouter) (otherHost.getRouter());
        if (m.getProperty(CONTENT_PROPERTY_INTEREST).equals(peer.getInterest())) {
            return true;
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
        return new EpidemicWithInterest(this);
    }

    private EpidemicWithInterest getOtherDecisionEngineRouter(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";
        return (EpidemicWithInterest) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public Set<String> getTombstone() {
        return tombstone;
    }

    @Override
    public void update(DTNHost thisHost) {
        
        for (Message message : thisHost.getMessageCollection()) {
            if (message.getTtl() <=0) {
                tombstone.add(message.getId());
            }
        }
    }
}
