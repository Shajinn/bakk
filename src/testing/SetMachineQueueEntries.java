
package testing;

import jade.content.AgentAction;
import jade.util.leap.List;

/**
 *
 * @author Shahin Mahmody
 */
public class SetMachineQueueEntries implements AgentAction{
    private List machineOrderEntries;

    public SetMachineQueueEntries() {
    }

    public SetMachineQueueEntries(List machineOrderEntries) {
        this.machineOrderEntries = machineOrderEntries;
    }

    public List getMachineOrderEntries() {
        return machineOrderEntries;
    }

    public void setMachineOrderEntries(List machineOrderEntries) {
        this.machineOrderEntries = machineOrderEntries;
    }
    
    
}
