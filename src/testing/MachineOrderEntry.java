
package testing;

import jade.content.Concept;
import ontology.tool.terms.SettingSheet;
import ontology.tool.terms.ToolRequirementList;

/**
 *
 * @author Shahin Mahmody
 */
public class MachineOrderEntry implements Concept{
        private String id;
        private SettingSheet settingSheet = null;
        private float projectedCost = 0;
        private float projectedTime = 0;
        private float projectedSavings = 0;
        private boolean tooled = false;
        private ToolRequirementList projectedUsedTools;
        
        private String ncProgramNr; //used to get the setting sheet 
        private float maxCost; //maximum monetary cost to execute the order 
        private float maxTime; //maximum number of minutes to execute the order
        private int quantity; //number of workpieces to manufacture
        private float latenessAllowanceFactor; //factor of how much the maximum time may be exceeded to reduce costs

    public MachineOrderEntry() {
        super();
    }

    public MachineOrderEntry(String id, ToolRequirementList projectedUsedTools, String ncProgramNr, float maxCost, float maxTime, int quantity, float latenessAllowanceFactor) {
        this.id = id;
        this.projectedUsedTools = projectedUsedTools;
        this.ncProgramNr = ncProgramNr;
        this.maxCost = maxCost;
        this.maxTime = maxTime;
        this.quantity = quantity;
        this.latenessAllowanceFactor = latenessAllowanceFactor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SettingSheet getSettingSheet() {
        return settingSheet;
    }

    public void setSettingSheet(SettingSheet settingSheet) {
        this.settingSheet = settingSheet;
    }

    public float getProjectedCost() {
        return projectedCost;
    }

    public void setProjectedCost(float projectedCost) {
        this.projectedCost = projectedCost;
    }

    public float getProjectedTime() {
        return projectedTime;
    }

    public void setProjectedTime(float projectedTime) {
        this.projectedTime = projectedTime;
    }

    public float getProjectedSavings() {
        return projectedSavings;
    }

    public void setProjectedSavings(float projectedSavings) {
        this.projectedSavings = projectedSavings;
    }

    public boolean getTooled() {
        return tooled;
    }

    public void setTooled(boolean tooled) {
        this.tooled = tooled;
    }

    public ToolRequirementList getProjectedUsedTools() {
        return projectedUsedTools;
    }

    public void setProjectedUsedTools(ToolRequirementList projectedUsedTools) {
        this.projectedUsedTools = projectedUsedTools;
    }

    public String getNcProgramNr() {
        return ncProgramNr;
    }

    public void setNcProgramNr(String ncProgramNr) {
        this.ncProgramNr = ncProgramNr;
    }

    public float getMaxCost() {
        return maxCost;
    }

    public void setMaxCost(float maxCost) {
        this.maxCost = maxCost;
    }

    public float getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(float maxTime) {
        this.maxTime = maxTime;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getLatenessAllowanceFactor() {
        return latenessAllowanceFactor;
    }

    public void setLatenessAllowanceFactor(float latenessAllowanceFactor) {
        this.latenessAllowanceFactor = latenessAllowanceFactor;
    }
    
    
        
}
