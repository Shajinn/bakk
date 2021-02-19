
package ontology.tool.terms;

import jade.content.Predicate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Information on assembled tools in storage
 * 
 * @author Shahin Mahmody
 */
public class StoredTools implements Predicate{
    private List toolInstances;
    
    public StoredTools(){
        super();
    }

    public StoredTools(List toolInstances) {
        this.toolInstances = toolInstances;
    }

    public List getToolInstances() {
        return (toolInstances == null) ? toolInstances = new ArrayList() : toolInstances;
    }

    public void setToolInstances(List toolInstances) {
        this.toolInstances = toolInstances;
    }
    
    
}
