
package ontology.tool.terms;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Command for a UI Agent to display a commissioned tool list
 *
 * @author Shahin Mahmody
 */
public class DisplayCommissionList implements AgentAction{
    private List toolInstances;
    
    public DisplayCommissionList(){
        super();
    }

    public DisplayCommissionList(List toolInstances) {
        this.toolInstances = toolInstances;
    }

    public List getToolInstances() {
        return (toolInstances == null) ? toolInstances = new ArrayList() : toolInstances;
    }

    public void setToolInstances(List toolInstances) {
        this.toolInstances = toolInstances;
    }
    
}
