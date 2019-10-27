/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SimScenario;
import core.UpdateListener;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.interest.GossipGoLwithInterestRouter;

/**
 *
 * @author Debora Issadha
 */
public class ResidueInterest extends Report implements MessageListener {

    public final static String CONTENT_PROPERTY_INTEREST = "interest";
    public final static String CONTENT_PROPERTY_COUNTER = "counter";
    private List<DTNHost> nodes;
    private Map<Message, Integer> messageResidue;
    private int nrofDroppedByTTL;
    private int nrofDroppedByCounter;
    private int nrofDroppedByBuffer;

    /**
     * Constructor.
     */
    public ResidueInterest() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        this.nodes = new ArrayList<>(SimScenario.getInstance().getHosts());
        this.messageResidue = new HashMap<>();
        this.nrofDroppedByTTL = 0;
        this.nrofDroppedByBuffer = 0;
        this.nrofDroppedByCounter = 0;
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
        Integer msgCounter = (Integer) m.getProperty(CONTENT_PROPERTY_COUNTER);
        int notHaveMessage = 0;
        if (dropped) {
            if (m.getTtl() <= 0) {
                this.nrofDroppedByTTL++;
                for (DTNHost node : nodes) {
                    Collection<Message> messageCollections = node.getMessageCollection();
                    Set<String> idMessages = new HashSet<>();
                    for (Message msg : messageCollections) {
                        idMessages.add(msg.getId());
                    }
                    String nodeInterest = node.getRouter().getInterest();
                    if (nodeInterest.equals(messageInterest)) {
                        if (!idMessages.contains(m.getId())) {
                            notHaveMessage++;
                            messageResidue.put(m, notHaveMessage);
                        }
                    }
                }
            } else {
                this.nrofDroppedByCounter++;
            }
        } else {
            this.nrofDroppedByBuffer++;
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
        Map<String, ArrayList<Integer>> tempResiduePerInterest = new HashMap<>();
        Map<String, Integer> residuePerInterest = new HashMap<>();
        ArrayList<Integer> listResidues;
        for (Map.Entry<Message, Integer> entry : messageResidue.entrySet()) {
            Message message = entry.getKey();
            Integer residues = entry.getValue();
            String interest = (String) message.getProperty(CONTENT_PROPERTY_INTEREST);
            if (!tempResiduePerInterest.containsKey(interest)) {
                listResidues = new ArrayList<>();
                listResidues.add(residues);
                tempResiduePerInterest.put(interest, listResidues);
            } else {
                listResidues = tempResiduePerInterest.get(interest);
                listResidues.add(residues);
                tempResiduePerInterest.replace(interest, listResidues);
            }
        }

        for (Map.Entry<String, ArrayList<Integer>> entry : tempResiduePerInterest.entrySet()) {
            String interest = entry.getKey();
            ArrayList<Integer> value = entry.getValue();
            int count = 0;
            for (Integer residues : value) {
                count += residues;
            }
            int avgResidue = count / value.size();
            residuePerInterest.put(interest, avgResidue);
        }

        write("Average Residue : ");
        for (Map.Entry<String, Integer> entry : residuePerInterest.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            write(key + " " + value);
        }
        String printText = "dropped by TTL: " + this.nrofDroppedByTTL
                + "\ndropped by Counter: " + this.nrofDroppedByCounter
                + "\ndropped by Buffer: " + this.nrofDroppedByBuffer;
        write(printText);
        super.done();
    }
}
//
//
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package report;
//
//import routing.interest.Tombstone;
//import core.DTNHost;
//import core.Message;
//import core.MessageListener;
//import core.Settings;
//import core.SimScenario;
//import core.UpdateListener;
//import java.util.*;
//import javax.swing.text.html.parser.DTDConstants;
//import routing.DecisionEngineRouter;
//import routing.MessageRouter;
//import routing.RoutingDecisionEngine;
//import static routing.DecisionEngineRouter.PUBSUB_NS;
//
///**
// *
// * @author Debora Issadha revised 19 July 2019
// * 
// */
//public class ResidueInterestReport extends Report implements MessageListener {
//
//    public final static String CONTENT_PROPERTY_INTEREST = "interest";
//    public final static String CONTENT_PROPERTY_COUNTER = "counter";
//
//    private int nrofDroppedByTTL;
//    private int nrofDroppedByBuffernCounter;
//    private int totResidue;
//
//    private List<DTNHost> nodes;
//    private Map<String, Double> perMessage;
//    private Map<String, Set<DTNHost>> HostInterest;
//    private Map<String, Integer> msgResidue;
//
//    /**
//     * Constructor
//     */
//    public ResidueInterestReport() {
//        init();
//    }
//
//    @Override
//    protected void init() {
//        super.init();
//        this.nodes = new ArrayList<>(SimScenario.getInstance().getHosts());
//        this.perMessage = new HashMap<>();
//        this.HostInterest = new HashMap<>();
//        this.msgResidue = new HashMap<>();
//        this.nrofDroppedByTTL = 0;
//        this.nrofDroppedByBuffernCounter = 0;
//        this.totResidue = 0;
//    }
//
//    @Override
//    public void newMessage(Message m) {
//        DTNHost thisHost = m.getFrom();
//        
//        String Interest = (String) m.getProperty(CONTENT_PROPERTY_INTEREST);
//        Set<DTNHost> nodeList = getNodeList(Interest);
//        nodeList.add(thisHost);
//        HostInterest.put(Interest, nodeList);
//    }
//
//    @Override
//    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
//    }
//
//    @Override
//    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
//        String messageInterest = (String) m.getProperty(CONTENT_PROPERTY_INTEREST);
//        
//        Set<DTNHost> interestNode = getNodeList(messageInterest);
//        
//        // if dropped by TTL (active router untuk penghapusan buffer ganti dari true menjadi false)
//        if (dropped) {
//            // increment, catat berapa jumlah yang terhapus oleh ttl
//            this.nrofDroppedByTTL++;
//            for (DTNHost node : nodes) {
//                Collection<Message> messageCollections = node.getMessageCollection();
//                
//                //edited
//                MessageRouter r = node.getRouter();
//                if (!(r instanceof DecisionEngineRouter)) {
//                    continue;
//                }
//                RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
//                if (!(de instanceof Tombstone)) {
//                    continue;
//                }
//                Tombstone tbs = (Tombstone) de;
//                Set<String> tomb = tbs.getTombstone();
//
//                String nodeInterest = node.getRouter().getInterest();
//                // cek nilai ketertarikan pada node
//                int notHaveMessage = 0;
//                if (nodeInterest.equals(messageInterest)) {
//                    // jika sama maka,
//                    // cek apakah pesan m ada pada tombstone
//                        if (!tomb.contains(m.getId())) {
//                            // tidak, increment
//                            notHaveMessage++;
//                            msgResidue.put(m.getId(), notHaveMessage);
//                        }
////                    }
//                }
//                
//                
//            }
//            
//            for (Map.Entry<String, Integer> entry : msgResidue.entrySet()) {
//                String key = entry.getKey();
//                Integer value = entry.getValue();
//                double average = value / interestNode.size(); 
//                System.out.println(average);
//                perMessage.put(m.getId(), average);
//            }
//            
//            
//        } else {
//            //increment catat berapa banyak pesan yang terhapus oleh buffer dan counter
//            this.nrofDroppedByBuffernCounter++;
//        }
//    }
//
//    @Override
//    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
//    }
//
//    @Override
//    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
//    }
//
//    @Override
//    public void done() {
//        Map<String, ArrayList<Integer>> tempResiduePerInterest = new HashMap<>();
//        Map<String, Double> residuePerInterest = new HashMap<>();
//        Map<String, Double> resRes = new HashMap<>();
//        ArrayList<Integer> listResidues;
//        double countRes = 0;
//        Map<String, Integer> residuePerMessage = new HashMap<>();
//        // untuk setiap pesan yang memiliki residue
////        for (Map.Entry<String, Integer> entry : messageResidue.entrySet()) {
////            String message = entry.getKey();
////            Integer residues = entry.getValue();
////            countRes += residues;
//////            residuePerMessage.put(message.getId(), residues);
////        }
//       
//        
//        //interest
////        for (Map.Entry<Message, Integer> entry : messageResidue.entrySet()) {
////            Message message = entry.getKey();
////            Integer residues = entry.getValue();
////            String interest = (String) message.getProperty(CONTENT_PROPERTY_INTEREST);
////            if (!tempResiduePerInterest.containsKey(interest)) {
////                listResidues = new ArrayList<>();
////                listResidues.add(residues);
////                tempResiduePerInterest.put(interest, listResidues);
////            } else {
////                listResidues = tempResiduePerInterest.get(interest);
////                listResidues.add(residues);
////                tempResiduePerInterest.replace(interest, listResidues);
////            }
////            residuePerMessage.put(message.getId(), residues);
////        }
//
////        write("AVERAGE RESIDUE PER INTEREST");
////        for (Map.Entry<String, ArrayList<Integer>> entry : tempResiduePerInterest.entrySet()) {
////            String interest = entry.getKey();
////            ArrayList<Integer> value = entry.getValue();
////            double count = 0;
////            for (Integer residues : value) {
////                count += residues;
////            }
////            double avgResidue = count / value.size();
////            residuePerInterest.put(interest, avgResidue);
//////            write(interest + "\t\t\t: " + avgResidue);
////        }
//
//        
////        for (Map.Entry<String, Integer> entry : residuePerMessage.entrySet()) {
////            String key = entry.getKey();
////            Integer value = entry.getValue();
////            
////        }
//
//        for (Double residue : perMessage.values()) {
//            countRes += residue;
//        }
//
//        double avgResidueFix = countRes / perMessage.size();
//        String printText1 = "\nAVERAGE RESIDUE\t\t: " + avgResidueFix
//                + "\n\nNUMBER OF DROPPED AND DELETED MESSAGES"
//                + "\nby TTL\t\t\t: " + this.nrofDroppedByTTL
//                + "\nby Buffer and Counter\t: " + this.nrofDroppedByBuffernCounter
//                + "\n\nTOTAL"
//                + "\nResidue\t\t\t: " + countRes
////                + "\nMessage with and without Residue\t\t\t: " + messageResidue.keySet().size()
//                + "\nMessage with Residue\t: " + perMessage.size();
//        write(printText1);
//
//        // cetak residue per message
//        write("\nRESIDUE PER MESSAGE");
//        for (Map.Entry<String, Double> entry : perMessage.entrySet()) {
//            String key = entry.getKey();
//            Double value = entry.getValue();
//            write(key + "\t\t\t: " + value);
//        }
//        super.done();
//    }
//    
//    private Set<DTNHost> getNodeList(String interest){
//        if (HostInterest.containsKey(interest)) {
//            return HostInterest.get(interest);
//        } else {
//            Set<DTNHost> nodeList = new HashSet<>();
//            return nodeList;
//        }
//    }
//}
