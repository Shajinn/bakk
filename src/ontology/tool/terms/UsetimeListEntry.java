
package ontology.tool.terms;

import jade.content.Concept;

/**
 * Tool usage information for a UsetimeList
 *
 * @author Shahin Mahmody
 */
public class UsetimeListEntry implements Concept{
    private String id;
    private ToolRequirementList toolRequirementList;
    private float machiningTime;
    private float previousOrderMachiningTime;

    public UsetimeListEntry() {
    }

    public UsetimeListEntry(String id, ToolRequirementList toolRequirementList, float machiningTime, float previousOrderMachiningTime) {
        this.id = id;
        this.toolRequirementList = toolRequirementList;
        this.machiningTime = machiningTime;
        this.previousOrderMachiningTime = previousOrderMachiningTime;
    }

    public float getPreviousOrderMachiningTime() {
        return previousOrderMachiningTime;
    }

    public void setPreviousOrderMachiningTime(float previousOrderMachiningTime) {
        this.previousOrderMachiningTime = previousOrderMachiningTime;
    }

    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ToolRequirementList getToolRequirementList() {
        return toolRequirementList;
    }

    public void setToolRequirementList(ToolRequirementList toolRequirementList) {
        this.toolRequirementList = toolRequirementList;
    }

    public float getMachiningTime() {
        return machiningTime;
    }

    public void setMachiningTime(float machiningTime) {
        this.machiningTime = machiningTime;
    }
}
