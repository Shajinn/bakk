
package ontology.tool.terms;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * List of tool requirements, along with 'requirementType' to help interpret incoming message's semantic content
 *
 * @author Shahin Mahmody
 */
public class ToolRequirementList implements Concept{
    public static final int TYPE_NONE = 0;
    public static final int TYPE_QUANTITY = 1;
    public static final int TYPE_LIFETIME = 2;
    public static final int TYPE_USETIME = 3;
    public static final int TYPE_TEMPLATE = 4;
    
    private List toolRequirements;
    private int requirementType;
    
    public ToolRequirementList() {
        super();
    }

    public ToolRequirementList(List toolRequirements, int requirementType) {
        this.toolRequirements = toolRequirements;
        this.requirementType = requirementType;
    }

    public List getToolRequirements() {
        return (toolRequirements == null) ? toolRequirements = new ArrayList() : toolRequirements;
    }

    public void setToolRequirements(List toolRequirements) {
        this.toolRequirements = toolRequirements;
    }

    public int getRequirementType() {
        return requirementType;
    }

    public void setRequirementType(int requirementType) {
        if(requirementType < 0 || requirementType > 2){
            requirementType = 0;
        }
        this.requirementType = requirementType;
    }
    
    
}
