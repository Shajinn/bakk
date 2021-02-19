
package ontology.order.terms;

import jade.content.AgentAction;

/**
 * Informs an agent of order rejection
 *
 * @author Shahin Mahmody
 */
public class OrderRejected implements AgentAction{
    private String reason;

    public OrderRejected() {
    }

    public OrderRejected(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    
    
}
