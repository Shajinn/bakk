
package testing;

import jade.content.AgentAction;

/**
 *
 * @author Shahin Mahmody
 */
public class StartMachineSetupInfoRequestTest implements AgentAction{
    private String ncProgramNr;
    private float maxCost;
    private float maxTime;
    private int quantity;
    private float latenessAllowanceFactor;
    
    public StartMachineSetupInfoRequestTest(){
        
    }

    public StartMachineSetupInfoRequestTest(String ncProgramNr, float maxCost, float maxTime, int quantity, float latenessAllowanceFactor) {
        this.ncProgramNr = ncProgramNr;
        this.maxCost = maxCost;
        this.maxTime = maxTime;
        this.quantity = quantity;
        this.latenessAllowanceFactor = latenessAllowanceFactor;
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
