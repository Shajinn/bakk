
package ontology.tool.terms;

import jade.content.Concept;

/**
 * Information on requirements for machine tools
 *
 * @author Shahin Mahmody
 */
public class ToolRequirementEntry implements Concept {
    private Tool tool;
    private float usetime;
    private int quantity;
    private float templateToolLife;
    
    public ToolRequirementEntry() {
        super();
    }

    public ToolRequirementEntry(Tool tool, float usetime, int quantity, float templateToolLife) {
        this.tool = tool;
        this.usetime = usetime;
        this.quantity = quantity;
        this.templateToolLife = templateToolLife;
    }

    public float getTemplateToolLife() {
        return templateToolLife;
    }

    public void setTemplateToolLife(float templateToolLife) {
        this.templateToolLife = templateToolLife;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public float getUsetime() {
        return usetime;
    }

    public void setUsetime(float usetime) {
        this.usetime = usetime;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
