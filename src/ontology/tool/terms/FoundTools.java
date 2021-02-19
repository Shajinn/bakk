
package ontology.tool.terms;

import jade.content.Predicate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * A list of available tools
 * 
 * @author Shahin Mahmody
 */
public class FoundTools implements Predicate{
    private List toolInstances;

    public FoundTools() {
    }

    public FoundTools(List toolInstances) {
        this.toolInstances = toolInstances;
    }

    public List getToolInstances() {
        return (toolInstances == null) ? toolInstances = new ArrayList() : toolInstances;
    }

    public void setToolInstances(List toolInstances) {
        this.toolInstances = toolInstances;
    }
    
    
}
