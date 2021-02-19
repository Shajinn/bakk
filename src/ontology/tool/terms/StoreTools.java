
package ontology.tool.terms;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Command to return assembled tools to storage
 *
 * @author Shahin Mahmody
 */
public class StoreTools implements AgentAction{
    private List toolInstances;
    private boolean justAssembled;

    public StoreTools(){
        super();
    }

    public StoreTools(List toolInstances, boolean justAssembled) {
        this.toolInstances = toolInstances;
        this.justAssembled = justAssembled;
    }

    public boolean getJustAssembled() {
        return justAssembled;
    }

    public void setJustAssembled(boolean justAssembled) {
        this.justAssembled = justAssembled;
    }

    public List getToolInstances() {
        return (toolInstances == null) ? toolInstances = new ArrayList() : toolInstances;
    }

    public void setToolInstances(List toolInstances) {
        this.toolInstances = toolInstances;
    }
}
