
package ontology.tool.terms;

import jade.content.Predicate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * List of tool setup times for scenarios
 *
 * @author Shahin Mahmody
 */
public class ToolSupplyTimes implements Predicate{
    private List setupTimeLists;

    public ToolSupplyTimes() {
    }

    public ToolSupplyTimes(List setupTimeLists) {
        this.setupTimeLists = setupTimeLists;
    }

    public List getSetupTimeLists() {
        return (setupTimeLists == null) ? setupTimeLists = new ArrayList() : setupTimeLists;
    }

    public void setSetupTimeLists(List setupTimeLists) {
        this.setupTimeLists = setupTimeLists;
    }
    
}
