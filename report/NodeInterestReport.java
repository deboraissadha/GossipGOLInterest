package report;

import core.DTNHost;
import core.UpdateListener;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;

/**
 *
 * @author Junandus Sijabat
 */
public class NodeInterestReport extends Report implements UpdateListener{
    public static String HEADER = "INTEREST PADA NODE";
    protected Map<DTNHost, String> ListInterest = new HashMap<>();
    private int counter = 1;
    public NodeInterestReport(){
        init();
    }
    
    @Override
    public void init() {
        super.init();
        write(HEADER);
    }
    
     @Override
    public void updated(List<DTNHost> hosts) {
        if(counter ==1){
            for (DTNHost host : hosts) {
                MessageRouter otherRouter = host.getRouter();
                DecisionEngineRouter epic = (DecisionEngineRouter)otherRouter;
                String other = epic.getInterest();
                if (!ListInterest.containsKey(host)){
                    ListInterest.put(host, other);
                }
            }
            counter++;
        }
    }
    
    @Override
    public void done()
    {
        write("Node"+'\t'+"Interest");
        for(Map.Entry<DTNHost, String> entry : ListInterest.entrySet())
        {
            DTNHost a=entry.getKey();
            Integer b=a.getAddress();

            write("" + b + '\t' + entry.getValue());
        }
        super.done();
    }
}