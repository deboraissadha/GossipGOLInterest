/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimScenario;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author SuryoDeboAyas
 */
public class AverageResidue extends Report implements MessageListener {
    
    public final static String CONTENT_PROPERTY_INTEREST = "interest";
    private List<DTNHost> nodes;
    private Map<Message, Integer> messageResidue;
    private int nrofDroppedByTTL;
    private int notHaveMessage;

    /**
     * Constructor.
     */
    public AverageResidue() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        this.nodes = new ArrayList<>(SimScenario.getInstance().getHosts());
        this.messageResidue = new HashMap<>();
        this.nrofDroppedByTTL = 0;
        this.notHaveMessage = 0;
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
        int messageTTL = m.getTtl();
        int notHaveMessage = 0;
        if (messageTTL == 0) {
            this.nrofDroppedByTTL++;
            for (DTNHost node : nodes) {
                Collection<Message> messageCollections = node.getMessageCollection();
                String nodeInterest = node.getRouter().getInterest();
                if (nodeInterest.equals(messageInterest)) {
                    if (!messageCollections.contains(m)) {
                        notHaveMessage++;
                    }
                }
            }
            messageResidue.put(m, notHaveMessage);
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
        Map<String,Integer> residuePerInterest = new HashMap<>();
        ArrayList<Integer> listResidues;
        for (Map.Entry<Message, Integer> entry : messageResidue.entrySet()) {
            Message message = entry.getKey();
            Integer residues = entry.getValue();
            String interest = (String) message.getProperty(CONTENT_PROPERTY_INTEREST);
            if (!tempResiduePerInterest.containsKey(interest)) {
                listResidues = new ArrayList<>();
                listResidues.add(residues);
                tempResiduePerInterest.put(interest,listResidues);
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
            write(key+" "+value);
        }
        String printText = "dropped by TTL: "+this.nrofDroppedByTTL+"\nNot Have Message: "+notHaveMessage;
        write(printText);
        super.done();
    }
}
