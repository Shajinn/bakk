
package ontology.order.terms;

import jade.content.AgentAction;

/**
 * Command to place an order
 *
 * @author Shahin Mahmody
 */
public class MakeOrder implements AgentAction{
    private Order order;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
