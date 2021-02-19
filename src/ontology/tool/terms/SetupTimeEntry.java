
package ontology.tool.terms;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Setup times for a tooling scenario
 *
 * @author Shahin Mahmody
 */
public class SetupTimeEntry implements Concept{
    private List timesList;
    private int position;

    public SetupTimeEntry() {
    }

    public SetupTimeEntry(List timesList, int position) {
        this.timesList = timesList;
        this.position = position;
    }

    public List getTimesList() {
        return (timesList == null) ? timesList = new ArrayList() : timesList;
    }

    public void setTimesList(List timesList) {
        this.timesList = timesList;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    
    
}
