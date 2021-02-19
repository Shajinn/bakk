
package ontology.order;

/**
 *
 * @author Shahin Mahmody
 */
public interface OrderDomainVocabulary {
    //~~~~~~~~~~~~~~~~~~~~~~~CONCEPTS~~~~~~~~~~~~~~~~~~~~~~~~~~
    //Order
    public static final String ORDER = "Order";
    public static final String ORDER_NCPROGRAMNR = "ncProgramNr";
    public static final String ORDER_MAXCOST = "maxCost";
    public static final String ORDER_MAXTIME = "maxTime";
    public static final String ORDER_QUANTITY = "quantity";
    public static final String ORDER_LATENESSALLOWANCEFACTOR = "latenessAllowanceFactor";
    
    public static final String PROPOSEDCOST = "ProposedCost";
    public static final String PROPOSEDCOST_COST = "cost";
    public static final String PROPOSEDCOST_DURATION = "duration";
    public static final String PROPOSEDCOST_ORDERID = "orderId";
    public static final String PROPOSEDCOST_SAVINGS = "savings"; 
    public static final String PROPOSEDCOST_POSITION = "position";
    
    //~~~~~~~~~~~~~~~~~~~~~~~ACTIONS~~~~~~~~~~~~~~~~~~~~~~~~~~
    //put in order
    public static final String MAKEORDER = "MakeOrder";
    public static final String MAKEORDER_ORDER = "order";
    //Order completed
    public static final String ORDER_COMPLETE = "OrderComplete";
    
    public static final String EVALUATEPROPOSALS = "EvaluateProposals";
    public static final String EVALUATEPROPOSALS_PROPOSALS = "proposals";
    
    public static final String ORDERREJECTED = "OrderRejected";
    public static final String ORDERREJECTED_REASON = "reason";
    
    //~~~~~~~~~~~~~~~~~~~~~~~PREDICATES~~~~~~~~~~~~~~~~~~~~~~~~~~
    public static final String COSTSTOMAKE = "CostsToMake";
    public static final String COSTSTOMAKE_ORDER = "order";
    public static final String COSTSTOMAKE_POSITION = "position";
    public static final String COSTSTOMAKE_COST = "cost";
    public static final String COSTSTOMAKE_TIME = "time";
    public static final String COSTSTOMAKE_COSTSAVED = "costSaved";
    
    //~~~~~~~~~~~~~~~~~~~~~~~SERVICES & PROTOCOLS~~~~~~~~~~~~~~~~~~~~~~~~~~
    public static final String PRODUCTION_SERVICE = "production_service";
    public static final String ORDER_NEGOTIATION_PROTOCOL = "OrderProtocol";
}
