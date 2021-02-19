
package ontology.tool.terms;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * List of tool usages for a particular scenario
 *
 * @author Shahin Mahmody
 */
public class UsetimeList implements Concept{
    private List toolUsages;
    private int position;
    
    public UsetimeList() {
        super();
    }

    public UsetimeList(List toolUsages, int position) {
        this.toolUsages = toolUsages;
        this.position = position;
    }

    public List getToolUsages() {
        return (toolUsages == null) ? toolUsages = new ArrayList() : toolUsages;
    }

    public void setToolUsages(List toolUsages) {
        this.toolUsages = toolUsages;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    
    
}
