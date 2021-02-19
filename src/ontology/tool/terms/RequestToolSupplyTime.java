
package ontology.tool.terms;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Request total setup time for tooling scenarios
 *
 * @author Shahin Mahmody
 */
public class RequestToolSupplyTime implements AgentAction{
    private List usetimeLists;

    public RequestToolSupplyTime() {
    }

    public RequestToolSupplyTime(List usetimeLists) {
        this.usetimeLists = usetimeLists;
    }

    public List getUsetimeLists() {
        return (usetimeLists == null) ? usetimeLists = new ArrayList() : usetimeLists;
    }

    public void setUsetimeLists(List usetimeLists) {
        this.usetimeLists = usetimeLists;
    }

    
}
