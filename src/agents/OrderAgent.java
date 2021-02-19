
package agents;

import jade.content.AgentAction;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.util.logging.Logger;
import ontology.order.*;
import ontology.order.terms.*;
import ontology.order.terms.EvaluateProposals;
import ontology.order.terms.ProposedCosts;

/**
 * Handles negotiation of manufacturing orders coming down from
 * the organisational layer with available machines. Tries to 
 * get the best deal given the constraints relayed from the ERP
 * systems.
 *
 * @author Shahin Mahmody
 */
public class OrderAgent extends Agent{
    private static final Logger logger = Logger.getLogger("MAS");
    private final Codec codec = new SLCodec();
    private final Ontology ontology = OrderDomainOntology.getInstance();
    //representation of the manufacturing order's requirements and constraints
    private Order order;
    //machine agent's identifier
    private AID toolMachine = null;

    @Override
    protected void setup() {
        System.out.println("Order Agent "+ getAID().getName() +" started");
        logger.info("Order Agent "+ getAID().getName() +" started");
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        
        //get order arguments from the command line
        Object[] args = getArguments();
        if (args != null && args.length >= 5) {
            String ncProgramNr = args[0].toString();
            try{
                int orderQuantity = Integer.parseInt(args[1].toString());
                float maxCost = Float.parseFloat(args[2].toString());
                float maxTime = Float.parseFloat(args[3].toString());
                float latenessAllowance = Float.parseFloat(args[4].toString());
            
                order = new Order(ncProgramNr,maxCost, maxTime, orderQuantity, latenessAllowance);
                logger.info("Order Agent "+ getAID().getName() +" parsed an order:"+
                "\n~~~~~NCPROGRAMNR: "+ ncProgramNr +
                "\n~~~~~QUANTITY: "+ orderQuantity + 
                "\n~~~~~MAX COST: "+ maxCost +
                "\n~~~~~MAX DELAY: "+ maxTime +
                "\n~~~~~LATENESS ALLOWANCE: "+ latenessAllowance);
        
            }catch(NumberFormatException e){
                System.out.println("Order Agent "+ getAID().getName()+" was supplied invalid arguments");
                logger.severe("Order Agent "+ getAID().getName()+" was supplied invalid arguments");
                doDelete();
            }
        }else{
            System.out.println("Order Agent "+ getAID().getName()+" started with too few arguments");
            logger.severe("Order Agent "+ getAID().getName()+" started with too few arguments");
            doDelete();
        }
        
        addBehaviour(new ProposalListener());
    }
    
    @Override
    protected void takeDown() {
        System.out.println("Order Agent "+ getAID().getName()+" is shutting down");
        logger.info("Order Agent "+ getAID().getName()+" is shutting down");
        super.takeDown(); 
    }
    
    /**
     * Waits for proposals and updated proposals by the machine,
     * comparing them against the order's requirments and replying
     * comfirmation or rejection. Also handles order completion or
     * failure messages, terminating the agent
     */
    private class ProposalListener extends CyclicBehaviour{
        private final String conversationId;
        
        public ProposalListener(){
            this.conversationId = OrderDomainVocabulary.ORDER_NEGOTIATION_PROTOCOL+System.currentTimeMillis();
        }
        
        @Override
        public void onStart() {
            if(toolMachine == null){
                //look for machines & tell them of your offer
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType(OrderDomainVocabulary.PRODUCTION_SERVICE);
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent,
                    template);
                    //We assume that there is only one in the model
                    if(result.length > 0){
                        toolMachine = result[0].getName();
                    }else{
                        logger.severe("Order Agent "+ getAID().getName()+" couldn't"
                                + " find any machines");
                        doDelete();
                    }
                }catch (FIPAException e) {
                    logger.severe("Order Agent "+ getAID().getName()+" had"
                            + " an exception occurred while searching for machines");
                    doDelete();
                }
            }
            //=========MESSAGE 1 SEND===========
            ACLMessage message = new ACLMessage(ACLMessage.CFP);
            message.setOntology(ontology.getName());
            message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            message.addReceiver(toolMachine);
            message.setConversationId(conversationId);
            message.setProtocol(OrderDomainVocabulary.ORDER_NEGOTIATION_PROTOCOL);
            try{
                MakeOrder makeOrder = new MakeOrder();
                makeOrder.setOrder(order);
                myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(),makeOrder));
                myAgent.send(message);
            }catch(Codec.CodecException | OntologyException e){
                logger.severe("Order Agent "+ getAID().getName()+" failed"
                            + " to send its call for proposal to the machine agent");
                doDelete();
            }
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                if(msg.getConversationId() == null || !msg.getConversationId().equals(conversationId) ||
                        msg.getProtocol() == null || !msg.getProtocol().equals(OrderDomainVocabulary.ORDER_NEGOTIATION_PROTOCOL)){
                    myAgent.send(reply);
                    return;
                }
                try{
                    AgentAction action;
                    switch (msg.getPerformative()) {
                        case ACLMessage.NOT_UNDERSTOOD:
                            logger.warning("Order Agent "+ getAID().getName()+
                                                    " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                            return;
                        case ACLMessage.PROPOSE:
                            action = (AgentAction)((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                            if(action instanceof EvaluateProposals){
                                //=========MESSAGE 8 RECEIVE===========
                                List proposals = ((EvaluateProposals) action).getProposals();
                                List acceptedProposals = new ArrayList();
                                Iterator iterator = proposals.iterator();
                                //evaluate every proposal
                                while(iterator.hasNext()){
                                    ProposedCosts proposedCosts = (ProposedCosts) iterator.next();
                                    if(proposedCosts.getCost() < order.getMaxCost()){
                                        if(proposedCosts.getDuration() < order.getMaxTime() ||
                                                (proposedCosts.getSavings() > 0 &&
                                                proposedCosts.getDuration() < order.getMaxTime() * order.getLatenessAllowanceFactor())){
                                            acceptedProposals.add(proposedCosts);
                                            logger.info("Order Agent "+ getAID().getName()+
                                                    " accepted a proposal: cost:"+String.format("%.2f", proposedCosts.getCost())+" ,time:"
                                                    +String.format("%.2f", proposedCosts.getDuration())+" ,savings:"+String.format("%.2f", proposedCosts.getSavings())+" ,position:"+proposedCosts.getPosition());
                                        }else{
                                            logger.info("Order Agent "+ getAID().getName()+
                                                    " rejected a proposal: time: " +String.format("%.2f", proposedCosts.getDuration())+
                                                    " max-time: "+order.getMaxTime() + " (this does account for savings & lateness allowance factor but isn't displayed here)");
                                        }
                                    }else{
                                        logger.info("Order Agent "+ getAID().getName()+
                                                " rejected a proposal: cost: " +String.format("%.2f", proposedCosts.getCost())+
                                                " max-cost: "+order.getMaxCost());
                                    }
                                }
                                //=========MESSAGE 9 SEND===========
                                if(acceptedProposals.size() > 0){
                                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                    myAgent.getContentManager().fillContent(reply, new Action(myAgent.getAID(), new EvaluateProposals(acceptedProposals)));
                                }else{
                                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                }
                            }
                            break;
                        case ACLMessage.INFORM:
                            action = (AgentAction)((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                            //=========MESSAGE 20 RECEIVE===========  
                            if(action instanceof OrderCompleted){
                                System.out.println("Order Agent "+ getAID().getName()+
                                        "'s order was just finished!");
                                logger.info("Order Agent "+ getAID().getName()+
                                                "'s order was completed successfully");
                                myAgent.doDelete();
                                return;
                            }
                            break;
                        case ACLMessage.FAILURE:
                            System.out.println("Order Agent "+ getAID().getName()+"'s order was rejected");
                            logger.info("Order Agent "+ getAID().getName()+
                                                    "'s proposal was rejected");
                            myAgent.doDelete();
                            return;
                        default:
                            break;
                    }   
                }catch(OntologyException | Codec.CodecException e){
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Order Agent "+ getAID().getName()+
                                                    " could not construct a proper reply to \n"+msg.getContent());
                }
                myAgent.send(reply);
            }else{
                block();
            }
        }
    }
}
