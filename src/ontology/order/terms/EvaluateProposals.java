
package ontology.order.terms;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Request for feedback on list of proposed order fulfilment scenarios
 *
 * @author Shahin Mahmody
 */
public class EvaluateProposals implements AgentAction{
    private List proposals;
    
    public EvaluateProposals() {
    }

    public EvaluateProposals(List proposals) {
        this.proposals = proposals;
    }

    public List getProposals() {
        return (proposals == null) ? proposals = new ArrayList() : proposals;
    }

    public void setProposals(List proposals) {
        this.proposals = proposals;
    }
    
    
}
