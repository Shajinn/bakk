
package ontology.tool.terms;

import jade.content.AgentAction;

/**
 * Action to start the Tool Requisition Protocol
 *
 * @author Shahin Mahmody
 */
public class MakeToolSupplyOrder implements AgentAction{
    private ToolRequirementList toolRequirements;

    public MakeToolSupplyOrder() {
    }

    public MakeToolSupplyOrder(ToolRequirementList toolRequirements) {
        this.toolRequirements = toolRequirements;
    }

    public ToolRequirementList getToolRequirements() {
        return toolRequirements;
    }

    public void setToolRequirements(ToolRequirementList toolRequirements) {
        this.toolRequirements = toolRequirements;
    }
}
