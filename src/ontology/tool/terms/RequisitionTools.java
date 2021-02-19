
package ontology.tool.terms;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Request for tools
 *
 * @author Shahin Mahmody
 */
public class RequisitionTools implements AgentAction{
    private List toolInstances;

    public RequisitionTools() {
    }

    public RequisitionTools(List toolInstances) {
        this.toolInstances = toolInstances;
    }

    public List getToolInstances() {
        return (toolInstances == null) ? toolInstances = new ArrayList() : toolInstances;
    }

    public void setToolInstances(List toolInstances) {
        this.toolInstances = toolInstances;
    }
    
    
}
