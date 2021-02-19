
package ontology.tool.terms;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Requests removal of tools
 *
 * @author Shahin Mahmody
 */
public class TakeOutTools implements AgentAction{
    private List toolInstances;
    
    public TakeOutTools(){
        super();
    }

    public TakeOutTools(List toolInstances) {
        this.toolInstances = toolInstances;
    }

    public List getToolInstances() {
        return (toolInstances == null) ? toolInstances = new ArrayList() : toolInstances;
    }

    public void setToolInstances(List toolInstances) {
        this.toolInstances = toolInstances;
    }
    
}
