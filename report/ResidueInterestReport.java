/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import routing.interest.Tombstone;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SimScenario;
//import core.UpdateListener;
import java.util.*;
//import javax.swing.text.html.parser.DTDConstants;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
//import static routing.DecisionEngineRouter.PUBSUB_NS;

/**
 *
 * @author Debora Issadha revised 19 July 2019
 *
 */
public class ResidueInterestReport extends Report implements MessageListener {

    public final static String CONTENT_PROPERTY_INTEREST = "interest";
    public final static String CONTENT_PROPERTY_COUNTER = "counter";

//    private int nrofNodeperM;
    private int nrofDroppedByTTL;
    private int nrofDroppedByBuffer;
    private int nrofDroppedByCounter;
//    private int nodeEqualsWithM;
    private int totResidue;

    private List<DTNHost> nodes;
    private Map<String, Integer> messageResidue;
    private Map<Map<String, Integer>, Integer> tempResidue;
//    private Map<String, Integer> nodeInterest;

    /**
     * Constructor.
     */
    public ResidueInterestReport() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        this.nodes = new ArrayList<>(SimScenario.getInstance().getHosts());
        this.messageResidue = new HashMap<>();
        this.tempResidue = new HashMap<>();
//        this.nrofNodeperM = 0;
        this.nrofDroppedByTTL = 0;
        this.nrofDroppedByBuffer = 0;
        this.nrofDroppedByCounter = 0;
//        this.nodeEqualsWithM = 0;
        this.totResidue = 0;
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
//                System.out.println("TTL");
                // increment, catat berapa jumlah yang terhapus karena TTL
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
//                        nrofNodeperM++;
                        // jika sama,
                        // cek apakah pesan m ada pada tombstone/
                        //if (!tomb.contains(m.getId())) {
                        if (!(tomb.contains(m.getId()) || r.hasMessage(m.getId())))
                                {
            // tidak ada, increment
                            notHaveMessage++;
                            messageResidue.put(m.getId(), notHaveMessage);
                        }
//                        tempResidue.put(messageResidue, nrofNodeperM);
                    }
                }
            } else {
                // Dropped by Buffer
                this.nrofDroppedByBuffer++;
//                System.out.println("drop buffer");
            }
        } else {
            //Deleted by Counter
            this.nrofDroppedByCounter++;
//            System.out.println("COUNTER");
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

        Settings s = new Settings();
        int nrofNode = s.getInt("Group.nrofHosts") - 1;

//        Map<String, ArrayList<Integer>> tempResiduePerInterest = new HashMap<>();
//        Map<String, Double> residuePerInterest = new HashMap<>();
//        ArrayList<Integer> listResidues;
//        Map<String, Integer> residuePerMessage = new HashMap<>();
// untuk setiap pesan yang memiliki residue
//        for (Map.Entry<String, Integer> entry : messageResidue.entrySet()) {
//            String message = entry.getKey();
//            Integer residues = entry.getValue();
//            residuePerMessage.put(message.getId(), residues);
//        }
        //interest
//        for (Map.Entry<Message, Integer> entry : messageResidue.entrySet()) {
//            Message message = entry.getKey();
//            Integer residues = entry.getValue();
//            String interest = (String) message.getProperty(CONTENT_PROPERTY_INTEREST);
//            if (!tempResiduePerInterest.containsKey(interest)) {
//                listResidues = new ArrayList<>();
//                listResidues.add(residues);
//                tempResiduePerInterest.put(interest, listResidues);
//            } else {
//                listResidues = tempResiduePerInterest.get(interest);
//                listResidues.add(residues);
//                tempResiduePerInterest.replace(interest, listResidues);
//            }
//            residuePerMessage.put(message.getId(), residues);
//        }
//        write("AVERAGE RESIDUE PER INTEREST");
//        for (Map.Entry<String, ArrayList<Integer>> entry : tempResiduePerInterest.entrySet()) {
//            String interest = entry.getKey();
//            ArrayList<Integer> value = entry.getValue();
//            double count = 0;
//            for (Integer residues : value) {
//                count += residues;
//            }
//            double avgResidue = count / value.size();
//            residuePerInterest.put(interest, avgResidue);
////            write(interest + "\t\t\t: " + avgResidue);
//        }
        double nrofRes = 0;
        for (Map.Entry<String, Integer> entry : messageResidue.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            nrofRes += value;
        }

//        double avgResidueFix = (nrofRes /nrofNode)/ this.nrofDroppedByTTL ;
        double avgResidueFix = nrofRes / this.nrofDroppedByTTL;
        String printText1 = "\nAVERAGE RESIDUE\t\t: " + avgResidueFix
                + "\n\nNUMBER OF DROPPED AND DELETED MESSAGES"
                + "\nby TTL\t\t\t: " + this.nrofDroppedByTTL
                + "\nby Buffer \t\t: " + this.nrofDroppedByBuffer
                + "\nby Counter\t\t: " + this.nrofDroppedByCounter
                + "\n\nTOTAL"
                + "\nResidue\t\t\t: " + nrofRes
                + "\nMessage with Residue\t: " + messageResidue.size();
        write(printText1);

        // cetak residue per message
        write("\nRESIDUE PER MESSAGE");
        for (Map.Entry<String, Integer> entry : messageResidue.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            write(key + "\t\t\t: " + value);
        }
        super.done();
    }
}
