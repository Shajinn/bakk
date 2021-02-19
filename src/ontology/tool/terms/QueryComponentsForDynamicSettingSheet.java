
package ontology.tool.terms;

import jade.content.AgentAction;
/**
 * Request picking list for a given net tool list
 * 
 * @author Shahin Mahmody
 */
public class QueryComponentsForDynamicSettingSheet implements AgentAction{
    private ToolRequirementList toolRequirementsList;

    public QueryComponentsForDynamicSettingSheet() {
    }

    public QueryComponentsForDynamicSettingSheet(ToolRequirementList toolRequirementsList) {
        this.toolRequirementsList = toolRequirementsList;
    }

    public ToolRequirementList getToolRequirementsList() {
        return toolRequirementsList;
    }

    public void setToolRequirementsList(ToolRequirementList toolRequirementsList) {
        this.toolRequirementsList = toolRequirementsList;
    }
}
