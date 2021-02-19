
package ontology.tool.terms;

import jade.content.Concept;

/**
 * Information on component quantity for a given tool type's assembly
 *
 * @author Shahin Mahmody
 */
public class PickingListEntry implements Concept{
    private Component component;
    private int quantity;
    private Tool tool;

    public PickingListEntry() {
    }

    public PickingListEntry(Component component, int quantity, Tool tool) {
        this.component = component;
        this.quantity = quantity;
        this.tool = tool;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    
    
}
