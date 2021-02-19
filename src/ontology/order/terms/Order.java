
package ontology.order.terms;

import jade.content.Concept;

/**
 * An order to the machines. <br/>
 * Contains the program to execute, quantity to produce
 * as well as the maximum costs in terms of money and time.
 * @author Shahin Mahmody
 */
public class Order implements Concept{
    private String ncProgramNr; //used to get the setting sheet 
    private float maxCost; //maximum monetary cost to execute the order 
    private float maxTime; //maximum number of minutes to execute the order
    private int quantity; //number of workpieces to manufacture
    private float latenessAllowanceFactor; //factor of how much the maximum time may be exceeded to reduce costs

    public Order(String ncProgramNr, float maxCost, float maxTime, int quantity, float latenessAllowanceFactor) {
        this.ncProgramNr = ncProgramNr;
        this.maxCost = maxCost;
        this.maxTime = maxTime;
        this.quantity = quantity;
        this.latenessAllowanceFactor = latenessAllowanceFactor;
    }

    public Order() {
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
