
package ontology.order;

import jade.content.onto.*;
import jade.content.schema.*;
import java.math.BigDecimal;
import java.util.logging.Logger;
import ontology.order.terms.*;

/**
 * The Ontology class for Agents that handle Orders
 *
 * @author Shahin Mahmody
 */
public class OrderDomainOntology extends Ontology implements OrderDomainVocabulary{
    public static final String ONTOLOGY_NAME = "orderDomainOntology";
    private static final Ontology instance = new OrderDomainOntology();
    private static final Logger logger = Logger.getLogger("MAS");
    
    public static final int FLOAT_SCALE = 100;
    public static final BigDecimal FLOAT_SCALE_MULTIPLIER = new BigDecimal(FLOAT_SCALE);
    
    private OrderDomainOntology(){
        super(ONTOLOGY_NAME, BasicOntology.getInstance());
        
        try{
            //~~~~~~~~~~~~~~~~~~~~~~~CONCEPTS~~~~~~~~~~~~~~~~~~~~~~~~~~
            //Order
            add(new ConceptSchema(OrderDomainVocabulary.ORDER),Order.class);
            ConceptSchema cs = (ConceptSchema) getSchema(OrderDomainVocabulary.ORDER);
            cs.add(OrderDomainVocabulary.ORDER_NCPROGRAMNR, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(OrderDomainVocabulary.ORDER_MAXCOST, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(OrderDomainVocabulary.ORDER_MAXTIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(OrderDomainVocabulary.ORDER_QUANTITY, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            cs.add(OrderDomainVocabulary.ORDER_LATENESSALLOWANCEFACTOR, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            
            add(new ConceptSchema(OrderDomainVocabulary.PROPOSEDCOST),ProposedCosts.class);
            cs = (ConceptSchema) getSchema(OrderDomainVocabulary.PROPOSEDCOST);
            cs.add(OrderDomainVocabulary.PROPOSEDCOST_COST, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(OrderDomainVocabulary.PROPOSEDCOST_DURATION, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(OrderDomainVocabulary.PROPOSEDCOST_ORDERID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(OrderDomainVocabulary.PROPOSEDCOST_POSITION, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            cs.add(OrderDomainVocabulary.PROPOSEDCOST_SAVINGS, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            
            //~~~~~~~~~~~~~~~~~~~~~~~ACTIONS~~~~~~~~~~~~~~~~~~~~~~~~~~
            add(new AgentActionSchema(OrderDomainVocabulary.MAKEORDER),MakeOrder.class);
            AgentActionSchema aas = (AgentActionSchema) getSchema(OrderDomainVocabulary.MAKEORDER);
            aas.add(OrderDomainVocabulary.MAKEORDER_ORDER, (ConceptSchema) getSchema(OrderDomainVocabulary.ORDER));
            
            add(new AgentActionSchema(ORDER_COMPLETE),OrderCompleted.class);
            
            add(new AgentActionSchema(EVALUATEPROPOSALS),EvaluateProposals.class);
            aas = (AgentActionSchema) getSchema(EVALUATEPROPOSALS);
            aas.add(OrderDomainVocabulary.EVALUATEPROPOSALS_PROPOSALS, (ConceptSchema) getSchema(OrderDomainVocabulary.PROPOSEDCOST),0, ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(ORDERREJECTED),OrderRejected.class);
            aas = (AgentActionSchema) getSchema(ORDERREJECTED);
            aas.add(OrderDomainVocabulary.ORDERREJECTED_REASON, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            
        }catch(OntologyException e){
            logger.severe("Order ontology could not be built");
        }
    }
        
    public static Ontology getInstance(){
        return instance;
    }
}
