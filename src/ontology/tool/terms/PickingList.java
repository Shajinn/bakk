
package ontology.tool.terms;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * A list of components for tool assembly
 *
 * @author Shahin Mahmody
 */
public class PickingList implements Concept{
    private List components;
    
    public PickingList() {
        super();
    }

    public PickingList(List components) {
        this.components = components;
    }

    public List getComponents() {
        return (components == null) ? components = new ArrayList() : components;
    }

    public void setComponents(List components) {
        this.components = components;
    }
    
    
}
