
package testing;

import jade.content.AgentAction;
import jade.util.leap.List;

/**
 *
 * @author Shahin Mahmody
 */
public class SetTools implements AgentAction{
    private List toolInstances;
    private boolean stored;
    
    public SetTools() {
        super();
    }
    
    public SetTools(List toolInstances, boolean stored) {
        this.toolInstances = toolInstances;
        this.stored = stored;
    }
    
    public SetTools(List toolInstances) {
        this.toolInstances = toolInstances;
        this.stored = true;
    }

    public List getToolInstances() {
        return toolInstances;
    }

    public void setToolInstances(List toolInstances) {
        this.toolInstances = toolInstances;
    }

    public boolean getStored() {
        return stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }
    
    
}
