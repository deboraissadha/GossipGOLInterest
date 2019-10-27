/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import routing.interest.Tombstone;
import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author Debora Issadha revised 19 July 2019
 *
 */
public class ResidueReport extends Report implements MessageListener {

    public final static String CONTENT_PROPERTY_INTEREST = "interest";
    public final static String CONTENT_PROPERTY_COUNTER = "counter";

    private double nrofDroppedByTTL;
    private double nrofDroppedByCounter;
    private double nrofDroppedByBuffer;
    private double totResidue;
    private double residue;

    private List<DTNHost> nodes;

    /**
     * Constructor.
     */
    public ResidueReport() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        this.nodes = new ArrayList<>(SimScenario.getInstance().getHosts());
        this.nrofDroppedByTTL = 0;
        this.nrofDroppedByCounter = 0;
        this.nrofDroppedByBuffer = 0;
        this.totResidue = 0;
        this.residue = 0;
    }

    @Override
    public void newMessage(Message m) {
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        String messageInterest = (String) m.getProperty(CONTENT_PROPERTY_INTEREST);
        int notHaveMessage = 0;
        // dropped (TTL or Buffer)
        if (dropped) {
            // Dropped by TTL
            if (m.getTtl() <= 0) {
                // increment, catat jumlah yang terhapus karena TTL
                this.nrofDroppedByTTL++;
                // untuk setiap node yang ada di jaringan
                for (DTNHost node : nodes) {
                    MessageRouter r = node.getRouter();
                    if (!(r instanceof DecisionEngineRouter)) {
                        continue;
                    }
                    RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
                    if (!(de instanceof Tombstone)) {
                        continue;
                    }
                    Tombstone tbs = (Tombstone) de;
                    Set<String> tomb = tbs.getTombstone();

                    String nodeInterest = node.getRouter().getInterest();
                    // cek nilai ketertarikan antar pesan m dengan node
                    if (nodeInterest.equals(messageInterest)) {
                        // jika sama,
                        // cek apakah pesan m ada pada tombstone/
                        if (!(tomb.contains(m.getId()) || r.hasMessage(m.getId()))) {
                            // tidak ada, increment
                            this.residue++;
                        }
                    }
                }
                this.totResidue = this.residue / this.nrofDroppedByTTL;
            } else {
                this.nrofDroppedByBuffer++;
            }
        } else {
            this.nrofDroppedByCounter++;
        }
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
    }

    @Override
    public void done() {
        String printText1 = "AVERAGE RESIDUE\t\t: " + this.totResidue
                + "\nResidue\t\t\t: " + this.residue
                + "\n\nNUMBER OF DROPPED AND DELETED MESSAGES"
                + "\nby TTL\t\t\t: " + this.nrofDroppedByTTL
                + "\nby Buffer \t\t: " + this.nrofDroppedByBuffer
                + "\nby Counter\t\t: " + this.nrofDroppedByCounter;
        write(printText1);
        super.done();
    }
}
