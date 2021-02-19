
package ontology.tool.terms;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Action to remove tools from knowledge base upon destruction
 *
 * @author Shahin Mahmody
 */
public class DeleteTools implements AgentAction{
    private List toolInstances;

    public DeleteTools() {
    }

    public DeleteTools(List toolInstances) {
        this.toolInstances = toolInstances;
    }

    public List getToolInstances() {
        return (toolInstances == null) ? toolInstances = new ArrayList() : toolInstances;
    }

    public void setToolInstances(List toolInstances) {
        this.toolInstances = toolInstances;
    }
    
    
    
}
