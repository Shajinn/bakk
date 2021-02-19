
package agents;

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
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
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;
import ontology.order.OrderDomainOntology;
import ontology.order.OrderDomainVocabulary;
import ontology.order.terms.*;
import ontology.tool.ToolDomainOntology;
import ontology.tool.ToolDomainVocabulary;
import ontology.tool.terms.*;
import testing.MachineOrderEntry;
import testing.MagazineSetupTestResponse;
import testing.SetMachineQueueEntries;
import testing.SetTools;
import testing.StartMachineSetupInfoRequestTest;
import testing.StartMagazineSetupTest;
import testing.StartToolRequisitionTest;

/**
 * The digital representation of a tool machine. It negotiates orders,
 * coordinates cost and time calculations. Contains a complex internal state
 * handled by its MachineAdministrationEngine
 *
 * @author Shahin Mahmody
 */
public class MachineAgent extends Agent{
    private static final Logger logger = Logger.getLogger("MAS");
    
    private static final float MACHINING_COST = 10; //what's the cost of having the machine run for 1 hour
    private static final float TOOL_COST = 10;
    private static final float AUXILIARY_COST = 5;
    
    private final Codec codec = new SLCodec();
    private final Ontology toolOntology = ToolDomainOntology.getInstance();
    private final Ontology orderOntology = OrderDomainOntology.getInstance();
    private AID supplyAgent = null;
    private AID dbAgent = null;
    private AID visualAgent = null;
    
    private MachineAdministrationEngine machineEngine;
    private ToolingHandler queuedToolingProcess = null;
    //incoming orders that have not been looked at yet
    private final LinkedList<ACLMessage> unprocessedOrders = new LinkedList<ACLMessage>();
    
    
    //test-related 
    private AID testingAgent = null;
    private String testConvo = null;
    
    @Override
    protected void setup() {
        logger.info("Machine Agent "+ getAID().getName() +" started");
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(orderOntology);
        getContentManager().registerOntology(toolOntology);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(OrderDomainVocabulary.PRODUCTION_SERVICE);
        sd.setName(getLocalName()+"-Machine");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            logger.severe("Machine Agent "+getAID().getName()+" failed to register its service");
            doDelete();
        }
        
        addBehaviour(new ProposalHandler());
        machineEngine = new MachineAdministrationEngine();
        addBehaviour(new TestListener());
    }
    
    @Override
    protected void takeDown() {
        System.out.println("Machine Agent "+getAID().getName()+" is shutting down");
        super.takeDown();
    }
    
    private void initiateToolingSetup(int position)throws OntologyException,CodecException{
        addBehaviour(new SetupHandler(position));
    }
    
    private void processNextOrder(){
        ACLMessage sourceMessage = unprocessedOrders.poll();
        if(sourceMessage != null){
            try{
                Order order = ((MakeOrder)((Action) getContentManager().extractContent(sourceMessage)).getAction()).getOrder();
                addBehaviour(new OrderCommunicator(order, sourceMessage));
            }catch(CodecException | OntologyException e){
               logger.warning("Machine Agent "+getAID().getName()+" could not construct a message starting the order protocol");
            }
        }
    }
    
    private void sendNextToolingRequest()throws OntologyException, CodecException {
        if(queuedToolingProcess != null){
            addBehaviour(queuedToolingProcess);
            queuedToolingProcess = null;
        }
    }
    
    /**
     * Listens for other Agents sending machining orders and replies with a proposal
     */
    private class ProposalHandler extends CyclicBehaviour{

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                
                try{    
                    AgentAction action  = (AgentAction)((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                    //=========MESSAGE 1 RECEIVE===========
                    if(action instanceof MakeOrder){
                        //get Setting Sheet
                        Order order = ((MakeOrder) action).getOrder();
                            
                        if(machineEngine.hasLockingOrder()){
                            unprocessedOrders.add(msg);
                            logger.info("Machine Agent "+ getAID().getName()+ " puts an order into the queue: currently processing different order!");
                        }else{
                            myAgent.addBehaviour(new OrderCommunicator(order, msg));
                        }
                        return;
                    }
                }catch(OntologyException | CodecException e){
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Machine Agent "+ getAID().getName()+
                                                    " could not construct a proper reply to \n"+msg.getContent());
                }
                myAgent.send(reply);
            }else{
                block();
            }
        }
    }
    
    /**
     * Handles the calculation of proposals for orders
     */
    private class OrderCommunicator extends CyclicBehaviour{
        private final String conversationId;
        private final Order order;
        private final ACLMessage originalMsg;
        private SettingSheet sSheet;
        private List projectedRequirementLists;
        private String currentFilter = ToolDomainVocabulary.QUERY_SETTINGSHEET;
        
        public OrderCommunicator(Order order, ACLMessage originalMsg){
            this.conversationId = ToolDomainVocabulary.MACHINE_SETUP_INFO_REQUEST_PROTOCOL+Long.toString(System.currentTimeMillis());
            this.order = order;
            this.originalMsg = originalMsg;
        }
        
        @Override
        public void onStart() {
            lookupAgents();
            machineEngine.setLockingOrder(order, null, originalMsg.getSender(), originalMsg.getConversationId());
            //=========MESSAGE 2 SEND===========
            try{
                ACLMessage settingSheetQuery = new ACLMessage(ACLMessage.QUERY_REF);
                settingSheetQuery.setOntology(toolOntology.getName());
                settingSheetQuery.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                settingSheetQuery.setConversationId(conversationId);
                settingSheetQuery.setProtocol(ToolDomainVocabulary.MACHINE_SETUP_INFO_REQUEST_PROTOCOL);
                settingSheetQuery.setReplyWith(ToolDomainVocabulary.QUERY_SETTINGSHEET);
                settingSheetQuery.addReceiver(dbAgent);
                QuerySettingSheet qss = new QuerySettingSheet(order.getNcProgramNr());
                myAgent.getContentManager().fillContent(settingSheetQuery, new Action(getAID(), qss));
                myAgent.send(settingSheetQuery);
            }catch(OntologyException | CodecException e){
               logger.warning("Machine Agent "+getAID().getName()+" could not construct a message requesting info on setup");
            }
        }
        
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(MessageTemplate.and(MessageTemplate.MatchConversationId(conversationId),
                    MessageTemplate.MatchProtocol(ToolDomainVocabulary.MACHINE_SETUP_INFO_REQUEST_PROTOCOL)));
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                try{
                    if(currentFilter.equals(msg.getInReplyTo())){
                        switch(msg.getPerformative()){
                            case ACLMessage.NOT_UNDERSTOOD:
                                logger.warning("Machine Agent "+ getAID().getName()+
                                                        " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                                return;
                            case ACLMessage.INFORM_REF:
                                ContentElement element = myAgent.getContentManager().extractContent(msg);
                                //=========MESSAGE 3 RECEIVE===========
                                if(element instanceof NcProgramReferencesSheet){
                                    sSheet = ((NcProgramReferencesSheet) element).getSettingSheet();

                                    machineEngine.setLockingOrderSettingSheet(sSheet);
                                    //=========MESSAGE 4 SEND===========
                                    ACLMessage queryMsg = new ACLMessage(ACLMessage.QUERY_REF);
                                    queryMsg.setOntology(toolOntology.getName());
                                    queryMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                    queryMsg.setReplyWith(ToolDomainVocabulary.QUERY_STOREDTOOLS);
                                    queryMsg.setConversationId(conversationId);
                                    queryMsg.setProtocol(ToolDomainVocabulary.MACHINE_SETUP_INFO_REQUEST_PROTOCOL);
                                    queryMsg.addReceiver(supplyAgent);

                                    currentFilter = ToolDomainVocabulary.QUERY_STOREDTOOLS;

                                    List tools = new jade.util.leap.ArrayList();
                                    for(Tool tool: machineEngine.getAllRequiredToolTypes(sSheet)){
                                        tools.add(tool);
                                        System.out.println(tool.getToolId() + ": " +tool.getName());
                                    }
                                    myAgent.getContentManager().fillContent(queryMsg, new Action(myAgent.getAID(),new QueryStoredTools(tools)));
                                    myAgent.send(queryMsg);
                                    return;
                                //=========MESSAGE 5 RECEIVE===========
                                }else if(element instanceof StoredTools){
                                    StoredTools storedTools = (StoredTools) element;
                                    
                                    List netToolLists = new jade.util.leap.ArrayList();
                                    if(machineEngine.getQueueSize() > 1){
                                        for(int i = 2; i < machineEngine.getQueueSize()+1; i++){
                                            List netToolList = machineEngine.getNetToolLists(i,storedTools.getToolInstances());
                                            if(netToolList != null){
                                                netToolLists.add(new UsetimeList(netToolList,i));
                                            }
                                        }
                                    }else{
                                        List netToolList = machineEngine.getNetToolLists(machineEngine.getQueueSize(),storedTools.getToolInstances());
                                        if(netToolList != null){
                                            netToolLists.add(new UsetimeList(netToolList,machineEngine.getQueueSize()));
                                        }
                                    }
                                    projectedRequirementLists = netToolLists;

                                    //=========MESSAGE 6 SEND===========
                                    ACLMessage queryMsg = new ACLMessage(ACLMessage.QUERY_REF);
                                    queryMsg.setOntology(toolOntology.getName());
                                    queryMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                    queryMsg.setConversationId(conversationId);
                                    queryMsg.setProtocol(ToolDomainVocabulary.MACHINE_SETUP_INFO_REQUEST_PROTOCOL);
                                    queryMsg.setReplyWith(ToolDomainVocabulary.REQUEST_TOOLSUPPLY_TIME);
                                    queryMsg.addReceiver(supplyAgent);

                                    currentFilter = ToolDomainVocabulary.REQUEST_TOOLSUPPLY_TIME;

                                    myAgent.getContentManager().fillContent(queryMsg, new Action(myAgent.getAID(),new RequestToolSupplyTime(netToolLists)));
                                    myAgent.send(queryMsg);
                                    return;
                                }else if(element instanceof ToolSupplyTimes){
                                    //=========MESSAGE 7 RECEIVE===========
                                    ToolSupplyTimes setupTimes = (ToolSupplyTimes) element;
                                    Iterator setupTimesListsIterator = setupTimes.getSetupTimeLists().iterator();
                                    HashMap<String,EvaluateProposals> proposalsMap = new HashMap<String,EvaluateProposals>();
                                    while(setupTimesListsIterator.hasNext()){
                                        SetupTimeEntry setupTimesEntry = (SetupTimeEntry) setupTimesListsIterator.next();
                                        Iterator setupTimesEntryIterator = setupTimesEntry.getTimesList().iterator();
                                        while(setupTimesEntryIterator.hasNext()){
                                            OrderSetupTime orderSetupTime = (OrderSetupTime) setupTimesEntryIterator.next();
                                            if(orderSetupTime.getTimeRequired() >= 0){
                                                float machiningCost =  machineEngine.getMachineOrderSettingSheet(orderSetupTime.getOrderId()).getThroughputTime() *
                                                        machineEngine.getMachineOrderOrder(orderSetupTime.getOrderId()).getQuantity()* MACHINING_COST / 60;
                                                
                                                float auxiliaryCost = machineEngine.getMachineOrderOrder(orderSetupTime.getOrderId()).getQuantity() *AUXILIARY_COST;
                                                float toolingCost = (orderSetupTime.getStoredToolsAmount() + orderSetupTime.getNewToolsAmount()) * TOOL_COST;
                                                float totalCost = machiningCost+auxiliaryCost+toolingCost;
                                                float savings = machineEngine.getMachineOrderCost(orderSetupTime.getOrderId()) - totalCost;
                                                //A completely new order always has savings equal to  0
                                                if(machineEngine.getMachineOrderCost(orderSetupTime.getOrderId()) == 0){
                                                    savings = 0;
                                                }
                                                //add together costs and send them as well as order duration and potential savings to the Order Agent
                                                ProposedCosts proposedCosts = new ProposedCosts(BigDecimal.valueOf(totalCost).setScale(3,RoundingMode.HALF_UP).floatValue(), 
                                                        orderSetupTime.getTimeRequired(), BigDecimal.valueOf(savings).setScale(3,RoundingMode.HALF_UP).floatValue(), 
                                                        orderSetupTime.getOrderId(),setupTimesEntry.getPosition());
                                                //orderId == original conversationId!
                                                proposalsMap.putIfAbsent(orderSetupTime.getOrderId(),new EvaluateProposals(new jade.util.leap.ArrayList()));
                                                proposalsMap.get(orderSetupTime.getOrderId()).getProposals().add(proposedCosts);
                                            }
                                        }
                                    }
                                    if(testingAgent == null){
                                        if(proposalsMap.keySet().isEmpty()){
                                            ACLMessage message = originalMsg.createReply();
                                            message.setPerformative(ACLMessage.FAILURE);
                                            myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), new OrderRejected("Setup time leads to stillstand on machine")));
                                            myAgent.send(message);
                                            machineEngine.releaseLockingOrder();
                                            processNextOrder();
                                            myAgent.removeBehaviour(this);
                                        }else{
                                            addBehaviour(new ProposalAnswerHandler(proposalsMap, projectedRequirementLists));
                                            myAgent.removeBehaviour(this);
                                        }
                                    }else{
                                        ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
                                        message.setOntology(toolOntology.getName());
                                        message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                        message.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                                        message.setConversationId(testConvo);
                                        message.addReceiver(testingAgent);
                                        myAgent.getContentManager().fillContent(message, setupTimes);
                                        myAgent.send(message);
                                        testingAgent = null;
                                        testConvo = null;
                                    }
                                    return;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }catch(OntologyException | CodecException e){
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Machine Agent "+ getAID().getName()+
                                                    " could not construct a proper reply to \n"+msg.getContent());
                }

                myAgent.send(reply);
            }else{
                block();
            }
        }
    }
    
    /**
     * Organises the communication with orders agents on new proposals
     */
    private class ProposalAnswerHandler extends CyclicBehaviour{
        private final ProposalInfo[] proposalInfos;
        private final ArrayList<ProposedCosts> allAcceptedProposals;
        private final List projectedRequirementsList;
        private final HashMap<String,EvaluateProposals> proposalsMap;
        private final String questionId;
        private final int recipientsNumber;
       
        public ProposalAnswerHandler(HashMap<String, EvaluateProposals> proposalsMap, List projectedRequirements){
            proposalInfos = new ProposalInfo[machineEngine.getQueueSize()+1];
            allAcceptedProposals = new ArrayList<ProposedCosts>();
            this.projectedRequirementsList = projectedRequirements;
            this.proposalsMap = proposalsMap;
            this.questionId = "ProposalUpdate"+System.currentTimeMillis();
            this.recipientsNumber = proposalsMap.size();
        }

        @Override
        public void onStart(){
            System.out.println("Machine Agent "+getAID().getName()+" is sending proposals to this many Order Agents: "+recipientsNumber);
            // send lists to the order agents
            try{
                //=========MESSAGE 8 SEND===========
                for(Map.Entry<String,EvaluateProposals> proposals: proposalsMap.entrySet()){
                    ACLMessage proposal = new ACLMessage(ACLMessage.PROPOSE);
                    proposal.setOntology(orderOntology.getName());
                    proposal.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                    proposal.setConversationId(proposals.getKey());
                    proposal.setProtocol(OrderDomainVocabulary.ORDER_NEGOTIATION_PROTOCOL);
                    proposal.setReplyWith(questionId);
                    proposal.addReceiver(machineEngine.getMachineOrderClient(proposals.getKey()));
                    myAgent.getContentManager().fillContent(proposal, new Action(myAgent.getAID(), proposals.getValue()));
                    myAgent.send(proposal);
                }
            }catch(OntologyException | CodecException e){
                logger.warning("Machine Agent "+getAID().getName()+" could not construct a message requesting evaluation of proposals");
            }

        }
        
        @Override
        public void action() {
             ACLMessage msg = myAgent.receive(MessageTemplate.and(MessageTemplate.MatchInReplyTo(questionId), MessageTemplate.or(
                     MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL))));
            if (msg != null && msg.getPerformative() != ACLMessage.NOT_UNDERSTOOD) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                try{
                    if(proposalsMap.containsKey(msg.getConversationId())){
                        if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                            //=========MESSAGE 9 RECEIVE===========
                            AgentAction action  = (AgentAction)((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                            List proposals = ((EvaluateProposals) action).getProposals();
                            if(proposals.isEmpty()){
                                sendRejection();
                                machineEngine.releaseLockingOrder();
                                processNextOrder();
                                myAgent.removeBehaviour(this);
                            }else{
                                Iterator proposalsIterator = proposals.iterator();
                                while(proposalsIterator.hasNext()){

                                    ProposedCosts proposedCosts = (ProposedCosts) proposalsIterator.next();
                                    allAcceptedProposals.add(proposedCosts);
                                    if(proposalInfos[proposedCosts.getPosition()] == null){
                                        proposalInfos[proposedCosts.getPosition()]= new ProposalInfo(proposedCosts.getPosition());
                                    }
                                    ProposalInfo proposalInfo = proposalInfos[proposedCosts.getPosition()];
                                    proposalInfo.acceptedByOrderAgentsCount++;
                                    proposalInfo.totalSavings += proposedCosts.getSavings();
                                    if(proposedCosts.getOrderId().equals(machineEngine.getLockingOrderId())){
                                        proposalInfo.costs = proposedCosts;
                                    }
                                }
                                proposalsMap.remove(msg.getConversationId());

                                if(proposalsMap.isEmpty()){
                                    ProposalInfo bestSavingsPosition = null;
                                    for(ProposalInfo proposalInfo: proposalInfos){
                                        //proposalInfo can be null since the first two slots are blocked if already occupied
                                        if(proposalInfo != null && proposalInfo.acceptedByOrderAgentsCount == recipientsNumber){
                                            if(bestSavingsPosition == null || bestSavingsPosition.totalSavings < proposalInfo.totalSavings){
                                                bestSavingsPosition = proposalInfo;
                                            }
                                        }
                                    }
                                    if(bestSavingsPosition == null){
                                        sendRejection();
                                        machineEngine.releaseLockingOrder();
                                        processNextOrder();
                                    }else{
                                        logger.info("Machine Agent "+getAID().getName()+" inserted the order from agent "+ 
                                                 machineEngine.getMachineOrderClient(machineEngine.getLockingOrderId()).getName()+
                                                " at position "+ bestSavingsPosition.position);
                                        for(ProposedCosts proposedCosts: allAcceptedProposals){
                                            if(proposedCosts.getPosition() == bestSavingsPosition.position){
                                                machineEngine.updateProjections(proposedCosts.getOrderId(), proposedCosts.getCost(), proposedCosts.getDuration());
                                            }
                                        }
                                        machineEngine.insertLockingOrderIntoQueue(bestSavingsPosition.position,
                                                bestSavingsPosition.costs.getCost(), bestSavingsPosition.costs.getDuration());
                                        processNextOrder();
                                        //update tooling requirements
                                        Iterator requirementsListIterator = projectedRequirementsList.iterator();
                                        while(requirementsListIterator.hasNext()){
                                            UsetimeList usetimeList = (UsetimeList) requirementsListIterator.next();
                                            if(usetimeList.getPosition() == bestSavingsPosition.position){
                                                //update 
                                                Iterator toolRequirementIterator = usetimeList.getToolUsages().iterator();
                                                while(toolRequirementIterator.hasNext()){
                                                    UsetimeListEntry usetimeListEntry = (UsetimeListEntry) toolRequirementIterator.next();
                                                    machineEngine.setMachineOrderProjectedToolRequirement(usetimeListEntry.getId(), usetimeListEntry.getToolRequirementList());
                                                }
                                            }
                                        }
                                        if(bestSavingsPosition.position < 2){
                                            if(bestSavingsPosition.position == 0 || machineEngine.isFirstOrderTooled()){
                                                initiateToolingSetup(bestSavingsPosition.position);
                                            }
                                        }
                                    }
                                    myAgent.removeBehaviour(this);
                                }
                            }
                            return;          
                        }else{
                            sendRejection();
                            machineEngine.releaseLockingOrder();
                            processNextOrder();
                            myAgent.removeBehaviour(this);
                            return;
                        }
                    }
                }catch(OntologyException | CodecException ex){
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Machine Agent "+ getAID().getName()+
                                                    " could not construct a proper reply to \n"+msg.getContent());
                }
                myAgent.send(reply);
            }else{
                block();
            }
        }
        
        private void sendRejection() throws CodecException, OntologyException{
            ACLMessage message = new ACLMessage(ACLMessage.FAILURE);
            message.setOntology(orderOntology.getName());
            message.setProtocol(OrderDomainVocabulary.ORDER_NEGOTIATION_PROTOCOL);
            message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            message.addReceiver(machineEngine.getMachineOrderClient(machineEngine.getLockingOrderId()));
            message.setConversationId(machineEngine.getLockingOrderId());
            myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), new OrderRejected("")));
            myAgent.send(message);
        }
       
        private class ProposalInfo{
            public int acceptedByOrderAgentsCount;
            public int position;
            public float totalSavings;
            public ProposedCosts costs;
            
            public ProposalInfo(int position){
                this.position = position;
                acceptedByOrderAgentsCount = 0;
                totalSavings = 0;
            }
        }
   }
   
   /**
    * Handles this agent's side of the tool requisition protocol
    */
   private class SetupHandler extends CyclicBehaviour{
        private final String conversationId;
        private final int position;
        
        public SetupHandler(int position){
            this.position = position;
            this.conversationId = ToolDomainVocabulary.TOOL_REQUISITION_PROTOCOL+System.currentTimeMillis();
        }
        
        @Override
        public void onStart() {
            lookupAgents();
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setOntology(toolOntology.getName());
            message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            message.addReceiver(supplyAgent);
            message.setConversationId(conversationId);
            message.setProtocol(ToolDomainVocabulary.TOOL_REQUISITION_PROTOCOL);
            try{
                getContentManager().fillContent(message, new Action(getAID(), 
                    new MakeToolSupplyOrder(machineEngine.getMachineOrderProjectedToolRequirement(
                            machineEngine.getMachineOrderIdByPosition(position)))));
            }catch(CodecException | OntologyException e){
                logger.warning("Machine Agent "+getAID().getName()+" could not construct a message requesting initiation of setup");
            }
            //=========MESSAGE 10 SEND===========
            send(message);
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(conversationId));
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                try{
                    AgentAction action  = (AgentAction)((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                    switch(msg.getPerformative()){
                        case ACLMessage.NOT_UNDERSTOOD:
                            logger.warning("Machine Agent "+ getAID().getName()+
                                                    " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                            return;
                        case ACLMessage.INFORM:
                            //=========MESSAGE 16.5 RECEIVE===========
                            if(action instanceof ToolInspectionFinished){
                                List finishedTools = ((ToolInspectionFinished) action).getToolInstances();
                                System.out.println("Machine Agent "+getAID().getName()+" was brought "+finishedTools.size()+ " finished tools");
                                reply.setPerformative(ACLMessage.REQUEST);
                                
                                if(queuedToolingProcess == null){
                                    queuedToolingProcess = new ToolingHandler(finishedTools);
                                }else{
                                    logger.warning("Machine Agent "+getAID().getName()+" wanted to initiate the next tooling process but there was none!");
                                    myAgent.removeBehaviour(this);
                                }
                                StringBuilder toolInstanceLog = new StringBuilder();
                                toolInstanceLog.append("Machine Agent ");
                                toolInstanceLog.append(getAID().getName());
                                toolInstanceLog.append(" was brought: \n");
                                for(Object o: finishedTools.toArray()){
                                    ToolInstance instance = (ToolInstance) o;
                                    toolInstanceLog.append(instance.getTool().getToolId());
                                    toolInstanceLog.append("     instance: ");
                                    toolInstanceLog.append(instance.getInstanceId() );
                                    toolInstanceLog.append("     life: ");
                                    toolInstanceLog.append(instance.getUsedTime());
                                    toolInstanceLog.append("\n");
                                }
                                logger.info(toolInstanceLog.toString());
                                
                                if(testingAgent == null){
                                    //Start tooling if this is the first in the queue
                                    if(!machineEngine.isFirstOrderTooled()){
                                        sendNextToolingRequest();
                                        if(machineEngine.getQueueSize() > 1){
                                            initiateToolingSetup(1);
                                        }
                                    }
                                }else{
                                    ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
                                    message.setOntology(toolOntology.getName());
                                    message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                    message.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                                    message.setConversationId(testConvo);
                                    message.addReceiver(testingAgent);
                                    myAgent.getContentManager().fillContent(message,new Action(myAgent.getAID(), action));
                                    myAgent.send(message);
                                    testingAgent = null;
                                    testConvo = null;
                                }
                                removeBehaviour(this);
                                return;
                            }
                            break;
                        case ACLMessage.FAILURE:
                            if(testingAgent == null){
                                logger.info("Machine Agent "+ getAID().getName() + " had supply cancel its order!");
                                ACLMessage message = new ACLMessage(ACLMessage.FAILURE);
                                message.setOntology(orderOntology.getName());
                                message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                message.addReceiver(machineEngine.getOrderingAgent(position));
                                message.setConversationId(machineEngine.getMachineOrderIdByPosition(position));
                                myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), new OrderRejected("")));
                                myAgent.send(message);
                            }else{
                                ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
                                message.setOntology(toolOntology.getName());
                                message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                message.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                                message.setConversationId(testConvo);
                                message.addReceiver(testingAgent);
                                myAgent.getContentManager().fillContent(message,new Action(myAgent.getAID(), action));
                                myAgent.send(message);
                                testingAgent = null;
                                testConvo = null;
                            }
                            myAgent.removeBehaviour(this);
                            return;
                        default:
                            break;
                    }
                }catch(OntologyException | Codec.CodecException e){
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Machine Agent "+ getAID().getName()+
                                                    " could not construct a proper reply to \n"+msg.getContent());
                }
                myAgent.send(reply);
            }else{
                block();
            }
        }
    }   
   
   /**
    * Handles this agent's side of the magazine setup protocol
    */
   private class ToolingHandler extends CyclicBehaviour{
        private final List toolsForMounting;
        private final String conversationId;

        public ToolingHandler(List toolsForMounting){
            this.toolsForMounting = toolsForMounting == null ?  new jade.util.leap.ArrayList() : toolsForMounting;
            this.conversationId = ToolDomainVocabulary.MAGAZINE_SETUP_PROTOCOL+System.currentTimeMillis();
        }

        @Override
        public void onStart() {
            lookupAgents();
            try{
                jade.util.leap.List toolsForTakeout = machineEngine.mountTools(toolsForMounting);
                if(toolsForTakeout == null){
                    logger.warning("Machine Agent "+getAID().getName()+" failed to tool: too many tools for the available slots");
                    if(testingAgent == null){
                        sendRejection();
                    }else{
                        ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
                        message.setOntology(toolOntology.getName());
                        message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                        message.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                        message.setConversationId(testConvo);
                        message.addReceiver(testingAgent);
                        List magazineTools = new jade.util.leap.ArrayList();
                        for(Object o: machineEngine.getMagazine().values()){
                            if(o != null){
                                magazineTools.add(o);
                            }
                        }
                        MagazineSetupTestResponse mstr = new MagazineSetupTestResponse(magazineTools);
                        mstr.setTooManyTools(true);
                        myAgent.getContentManager().fillContent(message, mstr);
                        myAgent.send(message);
                        testingAgent = null;
                        testConvo = null;
                    }
                    myAgent.removeBehaviour(this);
                    return;
                }
                
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.setOntology(toolOntology.getName());
                message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                message.addReceiver(visualAgent);
                message.setConversationId(conversationId);
                message.setProtocol(ToolDomainVocabulary.MAGAZINE_SETUP_PROTOCOL);
                getContentManager().fillContent(message, new Action(getAID(), new TakeOutTools(toolsForTakeout)));

                //=========MESSAGE 17 SEND===========
                send(message);
            } catch(OntologyException | CodecException e){
                logger.warning("Machine Agent "+getAID().getName()+" could not construct a message requesting tooling and execution");
            }
        }
       
        @Override
        public void action() {
            if(myAgent == null){
                return;
            }
            ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(conversationId));
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                try{
                    AgentAction action  = (AgentAction)((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                    switch(msg.getPerformative()){
                        case ACLMessage.NOT_UNDERSTOOD:
                            logger.warning("Machine Agent "+ getAID().getName()+
                                                    " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                            return;
                        case ACLMessage.INFORM:
                            //=========MESSAGE 18 RECEIVE===========
                            if(action instanceof SetupComplete){
                                System.out.println("Machine Agent "+getAID().getName()+" was told to start machining!");
                                //perform machining
                                ArrayList<ToolInstance> destroyedTools = machineEngine.executeOrder();
                                jade.util.leap.ArrayList destroyedToolsForMsg = new jade.util.leap.ArrayList();
                                
                                if(testingAgent == null){
                                    if(destroyedTools == null){
                                        logger.warning("Machine Agent "+getAID().getName()+" failed to process: too little lifetime left on tools!");
                                        sendRejection();
                                        myAgent.removeBehaviour(this);
                                    }else if(!destroyedTools.isEmpty()){
                                        for(ToolInstance tool: destroyedTools){
                                            destroyedToolsForMsg.add(tool);
                                        }

                                        ACLMessage destroyedToolsMessage = new ACLMessage(ACLMessage.REQUEST);
                                        destroyedToolsMessage.setOntology(toolOntology.getName());
                                        destroyedToolsMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                        destroyedToolsMessage.addReceiver(supplyAgent);
                                        getContentManager().fillContent(destroyedToolsMessage, new Action(getAID(), new DeleteTools(destroyedToolsForMsg))); 
                                        myAgent.send(destroyedToolsMessage);
                                    }
                                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                                    message.setOntology(orderOntology.getName());
                                    message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                    message.addReceiver(machineEngine.getOrderingAgent(0));
                                    message.setConversationId(machineEngine.getMachineOrderIdByPosition(0));
                                    message.setProtocol(OrderDomainVocabulary.ORDER_NEGOTIATION_PROTOCOL);
                                    getContentManager().fillContent(message, new Action(getAID(), new OrderCompleted()));
                                    //=========MESSAGE 20 SEND===========
                                    myAgent.send(message);
                                    machineEngine.adjustQueue();

                                    // start tooling and next machining order
                                    if(queuedToolingProcess != null){
                                        sendNextToolingRequest();
                                    }
                                    // start next order setup
                                    if(machineEngine.getQueueSize() > 1){
                                        initiateToolingSetup(1);
                                    }
                                }else{
                                    ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
                                    message.setOntology(toolOntology.getName());
                                    message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                    message.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                                    message.setConversationId(testConvo);
                                    message.addReceiver(testingAgent);
                                    MagazineSetupTestResponse mstr = new MagazineSetupTestResponse();
                                    
                                    if(destroyedTools == null){
                                        mstr.setTooLittleLifetime(true);
                                    }else{
                                        List magazineTools = new jade.util.leap.ArrayList();
                                        for(ToolInstance ti: machineEngine.getMagazine().values()){
                                            if(ti != null){
                                                magazineTools.add(ti);
                                            }
                                        }
                                        mstr.setMagazine(magazineTools);
                                    }
                                    myAgent.getContentManager().fillContent(message, mstr);
                                    myAgent.send(message);
                                    testingAgent = null;
                                    testConvo = null;
                                }
                                
                                myAgent.removeBehaviour(this);
                                return;
                            }
                            break;
                        default:
                            break;
                    }
                }catch(OntologyException | Codec.CodecException e){
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Machine Agent "+ getAID().getName()+
                                                    " could not construct a proper response to \n"+msg.getContent());
                }
                myAgent.send(reply);
            }else{
                block();
            }
        }
        
        private void sendRejection() throws CodecException, OntologyException{
            ACLMessage message = new ACLMessage(ACLMessage.FAILURE);
            message.setOntology(orderOntology.getName());
            message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            message.addReceiver(machineEngine.getOrderingAgent(0));
            message.setConversationId(conversationId);
            myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), new OrderRejected("")));
            myAgent.send(message);
        }
   }
   
   private class TestListener extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(MessageTemplate.MatchProtocol(ToolDomainVocabulary.TEST_PROTOCOL));
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                try{
                    AgentAction action  = (AgentAction)((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                    switch (msg.getPerformative()){
                        case ACLMessage.NOT_UNDERSTOOD:
                            logger.warning("Machine Agent "+ getAID().getName()+
                                                    " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                            return;
                        case ACLMessage.REQUEST:
                            if(action instanceof SetTools){
                                SetTools tools = (SetTools) action;
                                machineEngine.setMagazineTools(tools.getToolInstances());
                                getContentManager().fillContent(reply, new StoredTools());
                                reply.setPerformative(ACLMessage.CONFIRM);
                            }else if(action instanceof SetMachineQueueEntries){
                                SetMachineQueueEntries setEntries = ((SetMachineQueueEntries)action);
                                if(setEntries.getMachineOrderEntries() != null){
                                    Iterator entriesIterator = setEntries.getMachineOrderEntries().iterator();
                                    machineEngine.clearOrders();
                                    while(entriesIterator.hasNext()){
                                        MachineOrderEntry entry = ((MachineOrderEntry) entriesIterator.next());
                                        machineEngine.insertOrder(machineEngine.getQueueSize(), 
                                                new Order(entry.getId(),0,0,entry.getQuantity(),0), 
                                                entry.getSettingSheet(), null, entry.getId());
                                        if(machineEngine.getQueueSize() == 1 && entry.getTooled()){
                                            machineEngine.setFirstOrderTooled(true);
                                        }
                                        machineEngine.setMachineOrderProjectedToolRequirement(entry.getId(), entry.getProjectedUsedTools());
                                        machineEngine.calculateGrossRequirements(entry.getId());
                                    }
                                }else{
                                    machineEngine.clearOrders();
                                }
                                getContentManager().fillContent(reply, new Action(getAID(),new SetMachineQueueEntries(new jade.util.leap.ArrayList())));
                                reply.setPerformative(ACLMessage.CONFIRM);
                            }else if(action instanceof StartMachineSetupInfoRequestTest){
                                StartMachineSetupInfoRequestTest testAction = (StartMachineSetupInfoRequestTest) action;
                                myAgent.addBehaviour(new OrderCommunicator(
                                        new Order(testAction.getNcProgramNr(), testAction.getMaxCost(),
                                                testAction.getMaxTime(), testAction.getQuantity(), testAction.getLatenessAllowanceFactor()), msg));
                                testingAgent = msg.getSender();
                                testConvo = msg.getConversationId();
                                return;
                            }else if(action instanceof StartToolRequisitionTest){
                                StartToolRequisitionTest testAction = (StartToolRequisitionTest) action;
                                myAgent.addBehaviour(new SetupHandler(testAction.getPosition()));
                                testingAgent = msg.getSender();
                                testConvo = msg.getConversationId();
                                return;
                            }else if(action instanceof StartMagazineSetupTest){
                                StartMagazineSetupTest testAction = (StartMagazineSetupTest) action;
                                myAgent.addBehaviour(new ToolingHandler(testAction.getTools()));
                                testingAgent = msg.getSender();
                                testConvo = msg.getConversationId();
                                return;
                            }
                            break;
                        default:
                            break;
                            
                    }
                }catch (OntologyException | Codec.CodecException | ClassCastException e) {
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Machine Agent "+ getAID().getName()+
                                                    " could not construct a proper reply to \n"+msg.getContent());
                }
                myAgent.send(reply);
            }else{
                block();
            }
        }
    }
   
   private void lookupAgents(){
        if(supplyAgent == null){
            //find supply agent
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(ToolDomainVocabulary.SUPPLY_SERVICE);
            dfd.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(this,
                dfd);
                //We assume that there is only one in the model
                if(result.length > 0){
                    supplyAgent = result[0].getName();
                }else{
                    logger.severe("Machine Agent "+ getAID().getName()+" could"
                        + " not find a supply agent");
                    doDelete();
                }
            }catch (FIPAException e) {
                logger.severe("Machine Agent "+ getAID().getName()+" had"
                        + " an exception occurr while searching for supply agent");
                System.out.println(e.getMessage());
                doDelete();
            }
        }
        if(dbAgent == null){

            //find db agent
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(ToolDomainVocabulary.DATABASE_SERVICE);
            dfd.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(this,
                dfd);
                //We assume that there is only one in the model
                if(result.length > 0){
                    dbAgent = result[0].getName();
                }else{
                    logger.severe("Machine Agent "+ getAID().getName()+" could"
                        + " not find a database agent");
                    doDelete();
                }
            }catch (FIPAException e) {
                logger.severe("Machine Agent "+ getAID().getName()+" had"
                        + " an exception occurr while searching for database agent");
                System.out.println(e.getMessage());
                doDelete();
            }
        }
        if(visualAgent == null){
            //find visual agent
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(ToolDomainVocabulary.VISUAL_SERVICE);
            dfd.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(this,
                dfd);
                //We assume that there is only one in the model
                if(result.length > 0){
                    visualAgent = result[0].getName();
                }else{
                    logger.severe("Machine Agent "+ getAID().getName()+" could"
                        + " not find a visual agent");
                    doDelete();
                }
            }catch (FIPAException e) {
                logger.severe("Machine Agent "+ getAID().getName()+" had"
                        + " an exception occurr while searching for visual agent");
                System.out.println(e.getMessage());
                doDelete();
            }
        }
    }
}