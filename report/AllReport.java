package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;


/**
 *
 * @author Junandus Sijabat
 */
public class AllReport extends Report implements MessageListener{
    private int DeliveredInterest;
    private int nrofRelayed;
    private Map <String, ConvergenceData>ConvergenceTimeInterest;

    public AllReport(){
        init();
    }
    
    @Override
    protected void init(){
        super.init();
        DeliveredInterest = 0;
        nrofRelayed = 0;
        ConvergenceTimeInterest = new HashMap<String , ConvergenceData>();
    }
    @Override
    public void newMessage(Message m) {
        
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
        
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
    
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        nrofRelayed++;
        
        MessageRouter otherRouter = to.getRouter();
        DecisionEngineRouter epic = (DecisionEngineRouter) otherRouter;
        
        if(m.getProperty("interest").equals(epic.getInterest())){
            DeliveredInterest++;
            if (ConvergenceTimeInterest.containsKey(m.getId())) {
                ConvergenceData d = ConvergenceTimeInterest.get(m.getId());
                Set<DTNHost> nodeList = d.getNodeList();
                if (!nodeList.contains(to)) {
                    nodeList.add(to);
                    d.setNodeList(nodeList);
                    d.setConvergenceTime(d.getConvergenceTime() + (getSimTime() - m.getCreationTime()));
                    d.setLastNodeTime(getSimTime() - m.getCreationTime());
                    ConvergenceTimeInterest.replace(m.getId(), d);
                }
            } else {            
                ConvergenceData d = new ConvergenceData();
                d.setSource(from);
                d.setConvergenceTime(getSimTime() - m.getCreationTime());
                d.setLastNodeTime(getSimTime() - m.getCreationTime());
                Set<DTNHost> nodeList = new HashSet<>();
                nodeList.add(to);
                d.setNodeList(nodeList);
                ConvergenceTimeInterest.put(m.getId(), d);
            }
        }
    }
    @Override
     public void done() {
        Settings s = new Settings();
        int nrofNode = s.getInt("Group.nrofHosts") - 1;
        String report = "";
        /*write("Convergence Report\nSource Node\tMessage ID\tConvergenceTime\t\tLast Node Time\tNumber Of Node");
        for (Map.Entry<String, ConvergenceData> e : convergenceTime.entrySet()) {
            String m = e.getKey();
            ConvergenceData v = e.getValue();
            report = report+v.getSource()+"\t\t"+m+"\t\t"+v.getConvergenceTime()/v.getNodeList().size()+"\t"+v.getLastNodeTime()+"\t\t"+v.getNodeList().size()+"\n";
        }*/
        double avgConvTime = 0;
        double lastUpdateTime = 0;
        double nrofInfected = 0;
        for (Map.Entry<String, ConvergenceData> e : ConvergenceTimeInterest.entrySet()) {
            ConvergenceData v = e.getValue();
            avgConvTime += v.getConvergenceTime() / v.getNodeList().size();
            lastUpdateTime += v.getLastNodeTime();
            nrofInfected += v.getNodeList().size();
        }
        int TotalCopyForward = nrofRelayed - DeliveredInterest;
        report += "Message Relayed           = " + nrofRelayed + "\n"
                + "Average Convergence Time  = " + avgConvTime / ConvergenceTimeInterest.size() + "\n"
                + "Totl Delivered Interest   = " + DeliveredInterest + "\n"
                + "Total Relayed             = " + TotalCopyForward + "\n";
        write(report);
        super.done();
    }
}
