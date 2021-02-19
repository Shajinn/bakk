
package ontology.tool.terms;

import jade.content.Concept;

/**
 *
 * @author Shahin Mahmody
 */
public class OrderSetupTime implements Concept{
    private String orderId;
    private float timeRequired;
    private int newToolsAmount;
    private int storedToolsAmount;
    private ToolRequirementList toolRequirementDetails; 

    public OrderSetupTime() {
    }

    public OrderSetupTime(String orderId, float timeRequired, int newToolsAmount, int storedToolsAmount, ToolRequirementList toolRequirementDetails) {
        this.orderId = orderId;
        this.timeRequired = timeRequired;
        this.newToolsAmount = newToolsAmount;
        this.storedToolsAmount = storedToolsAmount;
        this.toolRequirementDetails = toolRequirementDetails;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public float getTimeRequired() {
        return timeRequired;
    }

    public void setTimeRequired(float timeRequired) {
        this.timeRequired = timeRequired;
    }

    public int getNewToolsAmount() {
        return newToolsAmount;
    }

    public void setNewToolsAmount(int newToolsAmount) {
        this.newToolsAmount = newToolsAmount;
    }

    public int getStoredToolsAmount() {
        return storedToolsAmount;
    }

    public void setStoredToolsAmount(int storedToolsAmount) {
        this.storedToolsAmount = storedToolsAmount;
    }

    public ToolRequirementList getToolRequirementDetails() {
        return toolRequirementDetails;
    }

    public void setToolRequirementDetails(ToolRequirementList toolRequirementDetails) {
        this.toolRequirementDetails = toolRequirementDetails;
    }
}
