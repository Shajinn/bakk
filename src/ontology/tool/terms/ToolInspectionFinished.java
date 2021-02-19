
package ontology.tool.terms;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Messages asserting completion of tool inspection for given tools
 *
 * @author Shahin Mahmody
 */
public class ToolInspectionFinished implements AgentAction{
    private List toolInstances;
    
    public ToolInspectionFinished(){
        super();
    }

    public ToolInspectionFinished(List toolInstances) {
        this.toolInstances = toolInstances;
    }

    public List getToolInstances() {
        return (toolInstances == null) ? toolInstances = new ArrayList() : toolInstances;
    }

    public void setToolInstances(List toolInstances) {
        this.toolInstances = toolInstances;
    }
    
}
