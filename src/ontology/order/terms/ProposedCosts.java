
package ontology.order.terms;

import jade.content.Concept;

/**
 * Cost and time information for a given spot in an Agent's queue
 *
 * @author Shahin Mahmody
 */
public class ProposedCosts implements Concept{
    private float cost;
    private float duration;
    private float savings;
    private String orderId;
    private int position;

    public ProposedCosts() {
    }

    public ProposedCosts(float cost, float duration, float savings, String orderId, int position) {
        this.cost = cost;
        this.duration = duration;
        this.savings = savings;
        this.orderId = orderId;
        this.position = position;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public float getSavings() {
        return savings;
    }

    public void setSavings(float savings) {
        this.savings = savings;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    
    
}
