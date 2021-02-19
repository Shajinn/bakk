
package ontology.tool.terms;

import jade.content.Concept;

/**
 * Setting sheet details
 *
 * @author Shahin Mahmody
 */
public class SettingSheet implements Concept{
    private String name;
    private String ncProgramNr;
    private float throughputTime;
    private float setupTime;
    private ToolRequirementList toolRequirementsList;
    
    public SettingSheet() {
        super();
    }

    public SettingSheet(String name, String ncProgramNr, float throughputTime, float setupTime, ToolRequirementList toolRequirementsList) {
        this.name = name;
        this.ncProgramNr = ncProgramNr;
        this.throughputTime = throughputTime;
        this.setupTime = setupTime;
        this.toolRequirementsList = toolRequirementsList;
    }

    public float getSetupTime() {
        return setupTime;
    }

    public void setSetupTime(float setupTime) {
        this.setupTime = setupTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNcProgramNr() {
        return ncProgramNr;
    }

    public void setNcProgramNr(String ncProgramNr) {
        this.ncProgramNr = ncProgramNr;
    }

    public float getThroughputTime() {
        return throughputTime;
    }

    public void setThroughputTime(float throughputTime) {
        this.throughputTime = throughputTime;
    }

    public ToolRequirementList getToolRequirementsList() {
        return toolRequirementsList;
    }

    public void setToolRequirementsList(ToolRequirementList toolRequirementsList) {
        this.toolRequirementsList = toolRequirementsList;
    }
    
    
}
