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
 * @author Junandus Sijabat edited by Debora Issadha
 */
public class AllReportInterest extends Report implements MessageListener {

    private int DeliveredInterest;
    private int nrofRelayed;
    private Map<String, ConvergenceData> ConvergenceTimeInterest;

    public AllReportInterest() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        DeliveredInterest = 0;
        nrofRelayed = 0;
        ConvergenceTimeInterest = new HashMap<String, ConvergenceData>();
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

        if (m.getProperty("interest").equals(epic.getInterest())) {
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
        }else{
            nrofRelayed++;
        }
    }

    @Override
    public void done() {
        Settings s = new Settings();
        int nrofNode = s.getInt("Group.nrofHosts") - 1;
        String report = "";

        double avgConvTime = 0;
        double lastUpdateTime = 0;
        double nrofInfected = 0;
        for (Map.Entry<String, ConvergenceData> e : ConvergenceTimeInterest.entrySet()) {
            ConvergenceData v = e.getValue();
            avgConvTime += v.getConvergenceTime() / v.getNodeList().size();
            lastUpdateTime += v.getLastNodeTime();
            nrofInfected += v.getNodeList().size();
        }
        // total copy forward adalah relay pesan yang dikirim ke node relay bukan ke node tujuan, alias cost
        int TotalCopyForward = nrofRelayed - DeliveredInterest;
        report += "MESSAGE RELAYED\t\t: " + nrofRelayed + "\n"
                + "\nTOTAL\n"
                + "DELIVERED INTEREST\t: " + DeliveredInterest + "\n"
                + "RELAYED MESSAGES\t: " + TotalCopyForward + "\n"
                + "\nAVG CONVERGENCE TIME\t: " + avgConvTime / ConvergenceTimeInterest.size() + "\n";
        write(report);
        super.done();
    }
}
