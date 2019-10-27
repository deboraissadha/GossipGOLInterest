/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;

/**
 *
 * @author Jarkom
 */
public class GossipGOLReport extends Report implements MessageListener{
    
    public final static String CONTENT_PROPERTY_COUNTER = "counter";

    private int nrofRelayed;
    
    private int nrofDroppedByTTL;
    private int nrofDroppedByBuffer;
    private int nrofDroppedByCounter;
    
    /**
     * Constructor.
     */
    public GossipGOLReport() {
        init();
    }
    
    @Override
    protected void init() {
        super.init();
        this.nrofRelayed = 0;
        
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
        if (dropped){
            if (m.getTtl() <= 0){
//                System.out.println("TTL");
                this.nrofDroppedByTTL++;
            }else{
//                System.out.println("BUFFER");
                this.nrofDroppedByBuffer++;
            }
        }else{
//                System.out.println("COUNTER");
            this.nrofDroppedByCounter++;
        }
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        this.nrofRelayed++;
    }
    
    @Override
    public void done() {
        String printText1 = "Relayed Messages\t: " + this.nrofRelayed
                + "\n\nNUMBER OF DROPPED AND DELETED MESSAGES"
                + "\nby TTL\t\t\t: " + this.nrofDroppedByTTL
                + "\nby Buffer \t\t: " + this.nrofDroppedByBuffer
                + "\nby Counter\t\t: " + this.nrofDroppedByCounter;
        write(printText1);
        super.done();
    }
}

