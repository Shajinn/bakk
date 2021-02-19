package agents;

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.Predicate;
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
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.logging.Logger;
import ontology.tool.ToolDomainOntology;
import ontology.tool.ToolDomainVocabulary;
import ontology.tool.terms.DisplayPickingList;
import ontology.tool.terms.DisplayCommissionList;
import ontology.tool.terms.OrderSetupTime;
import ontology.tool.terms.MakeToolSupplyOrder;
import ontology.tool.terms.PickingList;
import ontology.tool.terms.GeneratedPickingList;
import ontology.tool.terms.DeleteTools;
import ontology.tool.terms.FoundTools;
import ontology.tool.terms.QueryComponentsForDynamicSettingSheet;
import ontology.tool.terms.RequestToolSupplyTime;
import ontology.tool.terms.QueryStoredTools;
import ontology.tool.terms.RequisitionTools;
import ontology.tool.terms.SetupTimeEntry;
import ontology.tool.terms.ToolSupplyTimes;
import ontology.tool.terms.StoreTools;
import ontology.tool.terms.StoredTools;
import ontology.tool.terms.ToolAssemblyFinished;
import ontology.tool.terms.ToolInspectionFinished;
import ontology.tool.terms.ToolInstance;
import ontology.tool.terms.ToolRequirementEntry;
import ontology.tool.terms.ToolRequirementList;
import ontology.tool.terms.UsetimeList;
import ontology.tool.terms.UsetimeListEntry;

/**
 * Simulates the work of storage systems/workers in supplying both 
 * calculations of setup times as well as tool requisition and transport
 * services
 *
 * @author Shahin Mahmody
 */
public class SupplyAgent extends Agent {

    private static final Logger logger = Logger.getLogger("MAS");
    private static final float TOOL_ASSEMBLY_TIME = 5;
    private static final float TOOL_MEASUREMENT_TIME = 2;

    private final Codec codec = new SLCodec();
    private final Ontology ontology = ToolDomainOntology.getInstance();

    private AID dbAgent = null;
    private AID visualAgent = null;

    @Override
    protected void setup() {
        logger.info("Supply Agent " + getAID().getName() + " started");
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ToolDomainVocabulary.SUPPLY_SERVICE);
        sd.setName(getLocalName() + "-Supply");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            logger.severe("Supply Agent " + getAID().getName() + " failed to register its service");
            doDelete();
        }

        addBehaviour(new SupplyBasicListener());
    }

    @Override
    protected void takeDown() {
        logger.info("Supply Agent " + getAID().getName() + " is shutting down");
        super.takeDown();
    }

    /**
     * Listens to basic commands outside of protocols
     */
    private class SupplyBasicListener extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                try {
                    AgentAction action = (AgentAction) ((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                    switch (msg.getPerformative()) {
                        case ACLMessage.NOT_UNDERSTOOD:
                            logger.warning("Supply Agent " + getAID().getName()
                                    + " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                            return;
                        case ACLMessage.QUERY_REF:
                            lookupAgents();
                            if (action instanceof QueryStoredTools) {
                                //=========MESSAGE 4 RECEIVE===========/
                                ACLMessage message = new ACLMessage(ACLMessage.QUERY_REF);
                                message.setOntology(ontology.getName());
                                message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                message.addReceiver(dbAgent);
                                message.setConversationId(msg.getConversationId());

                                myAgent.addBehaviour(new QueryResultListener(msg.getConversationId(), msg));

                                myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), action));
                                myAgent.send(message);
                                return;
                            } else if (action instanceof RequestToolSupplyTime) {
                                //=========MESSAGE 6 RECEIVE===========/
                                List usetimeLists = ((RequestToolSupplyTime) action).getUsetimeLists();
                                ToolSupplyTimes setupTimeForTools = new ToolSupplyTimes(new ArrayList());
                                Iterator usetimeListIterator = usetimeLists.iterator();
                                while (usetimeListIterator.hasNext()) {
                                    UsetimeList usetimeList = (UsetimeList) usetimeListIterator.next();
                                    SetupTimeEntry setupTimeEntry = new SetupTimeEntry(new ArrayList(), usetimeList.getPosition());
                                    setupTimeForTools.getSetupTimeLists().add(setupTimeEntry);
                                    Iterator usetimeEntryIterator = usetimeList.getToolUsages().iterator();
                                    float totalTime = 0;
                                    while (usetimeEntryIterator.hasNext()) {
                                        UsetimeListEntry usetimeEntry = (UsetimeListEntry) usetimeEntryIterator.next();
                                        OrderSetupTime orderSetupTime = new OrderSetupTime();
                                        orderSetupTime.setOrderId(usetimeEntry.getId());
                                        orderSetupTime.setToolRequirementDetails(usetimeEntry.getToolRequirementList());
                                        setupTimeEntry.getTimesList().add(orderSetupTime);
                                        float setupTime = 0;
                                        int totalStoredTools = 0;
                                        int totalNewTools = 0;
                                        if (usetimeEntry.getToolRequirementList().getToolRequirements() != null) {
                                            Iterator toolRequirementIterator = usetimeEntry.getToolRequirementList().getToolRequirements().iterator();
                                            while (toolRequirementIterator.hasNext()) {
                                                ToolRequirementEntry tre = (ToolRequirementEntry) toolRequirementIterator.next();

                                                int newTools = (int) Math.ceil(tre.getUsetime()
                                                        / ((tre.getTool().getLifetime() - tre.getTool().getCriticaltime())));
                                                setupTime += (tre.getQuantity() + newTools) * TOOL_MEASUREMENT_TIME;
                                                setupTime += newTools * TOOL_ASSEMBLY_TIME;
                                                totalStoredTools += tre.getQuantity();
                                                totalNewTools += newTools;
                                            }
                                        }
                                        //if it' the first order, add setuptime
                                        if (totalTime == 0) {
                                            totalTime += setupTime;
                                        }
                                        if (usetimeEntry.getPreviousOrderMachiningTime() == 0
                                                || setupTime <= usetimeEntry.getPreviousOrderMachiningTime()) {
                                            totalTime += usetimeEntry.getMachiningTime();
                                            orderSetupTime.setTimeRequired(BigDecimal.valueOf(totalTime).setScale(3, RoundingMode.HALF_UP).floatValue());
                                        } else {
                                            // We don't want the machine to not be producing: 
                                            //if setup takes longer than previous order, reject!
                                            setupTimeForTools.getSetupTimeLists().remove(setupTimeForTools.getSetupTimeLists().size() - 1);
                                            logger.warning("Supply Agent " + getAID().getName() + " rejected an order which takes too long to set up");
                                        }
                                        orderSetupTime.setStoredToolsAmount(totalStoredTools);
                                        orderSetupTime.setNewToolsAmount(totalNewTools);
                                    }
                                }
                                reply.setPerformative(ACLMessage.INFORM_REF);
                                myAgent.getContentManager().fillContent(reply, setupTimeForTools);
                                //=========MESSAGE 7 SEND===========/
                            }
                            break;
                        case ACLMessage.REQUEST:
                            //=========MESSAGE 10 RECEIVE===========  
                            if (action instanceof MakeToolSupplyOrder) {
                                ToolRequirementList toolsList = ((MakeToolSupplyOrder) action).getToolRequirements();
                                MachineListener machineListener = new MachineListener(msg, toolsList);
                                myAgent.addBehaviour(machineListener);
                                return;
                            } else if (action instanceof StoreTools) {
                                ACLMessage message = new ACLMessage(msg.getPerformative());
                                message.addReceiver(dbAgent);
                                message.setOntology(ontology.getName());
                                message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), action));
                                myAgent.send(message);
                                return;
                            } else if (action instanceof DeleteTools) {
                                //=========MESSAGE 19 RECEIVE===========
                                ACLMessage message = new ACLMessage(msg.getPerformative());
                                message.addReceiver(dbAgent);
                                message.setOntology(ontology.getName());
                                message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), action));
                                myAgent.send(message);
                                return;
                            }
                            break;
                        default:
                            break;
                    }
                } catch (OntologyException | Codec.CodecException | ClassCastException e) {
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Supply Agent " + getAID().getName()
                            + " could not construct a proper reply to \n" + msg.getContent());
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    /**
     * Handles the SupplyAgent's side of the Tool Requisition Protocol
     */
    private class MachineListener extends CyclicBehaviour {

        private final ToolRequirementList toolRequirements;
        private List assembledTools = null;
        private final ACLMessage triggeringMessage;
        private final String conversationId;
        private String currentFilter = ToolDomainVocabulary.QUERY_COMPONENTS_FOR_DYNAMIC_SETTINGSHEET;

        public MachineListener(ACLMessage triggeringMessage, ToolRequirementList toolRequirements) {
            this.triggeringMessage = triggeringMessage;
            this.toolRequirements = toolRequirements;
            this.conversationId = ToolDomainVocabulary.SETUP_INFORMATION_PROTOCOL + System.currentTimeMillis();
        }

        @Override
        public void onStart() {
            lookupAgents();
            ToolRequirementList copiedList = new ToolRequirementList(new ArrayList(), ToolRequirementList.TYPE_QUANTITY);
            List toolEntries = toolRequirements.getToolRequirements();
            Iterator iterator = toolEntries.iterator();
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Supply Agent ");
            logBuilder.append(getAID().getName());
            logBuilder.append(" has received a dynamic setting sheet for the following tools: \n");
            while (iterator.hasNext()) {
                ToolRequirementEntry te = (ToolRequirementEntry) iterator.next();
                int newToolsCount = Math.max(0, (int) (Math.ceil(te.getUsetime()
                        / (te.getTool().getLifetime() - te.getTool().getCriticaltime()))));
                logBuilder.append(te.getTool().getToolId());
                logBuilder.append("     stored tools: ");
                logBuilder.append(te.getQuantity());
                logBuilder.append("     new tools: ");
                logBuilder.append(newToolsCount);
                logBuilder.append("\n");
                //replace tool lifetime with number of required tools
                copiedList.getToolRequirements().add(new ToolRequirementEntry(te.getTool(), 0, newToolsCount, 0));
            }
            logger.info(logBuilder.toString());

            ACLMessage message = new ACLMessage(ACLMessage.QUERY_REF);
            message.setOntology(ontology.getName());
            message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            message.setConversationId(conversationId);
            message.setProtocol(ToolDomainVocabulary.SETUP_INFORMATION_PROTOCOL);
            message.setReplyWith(ToolDomainVocabulary.QUERY_COMPONENTS_FOR_DYNAMIC_SETTINGSHEET);
            //make a request to the db
            message.addReceiver(dbAgent);

            QueryComponentsForDynamicSettingSheet queryPickingList = new QueryComponentsForDynamicSettingSheet(copiedList);
            try {
                //=========MESSAGE 11 SEND===========  
                myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), queryPickingList));
                send(message);
            } catch (OntologyException | Codec.CodecException e) {
                logger.warning("Supply Agent " + getAID().getName() + " could not construct an initiating message");
                myAgent.removeBehaviour(this);
            }
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(MessageTemplate.and(MessageTemplate.MatchConversationId(conversationId),
                    MessageTemplate.MatchProtocol(ToolDomainVocabulary.SETUP_INFORMATION_PROTOCOL)));
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                try {
                    if (currentFilter.equals(msg.getInReplyTo())) {
                        switch (msg.getPerformative()) {
                            case ACLMessage.NOT_UNDERSTOOD:
                                logger.warning("Supply Agent " + getAID().getName()
                                        + " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                                return;
                            case ACLMessage.INFORM_REF:
                                Predicate predicate = ((Predicate) myAgent.getContentManager().extractContent(msg));
                                //=========MESSAGE 12 RECEIVE=========== 
                                if (predicate instanceof GeneratedPickingList) {
                                    PickingList pickingList = ((GeneratedPickingList) predicate).getPickingList();
                                    ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                                    message.setOntology(ontology.getName());
                                    message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                    message.addReceiver(visualAgent);
                                    message.setConversationId(conversationId);
                                    message.setProtocol(ToolDomainVocabulary.SETUP_INFORMATION_PROTOCOL);
                                    message.setReplyWith(ToolDomainVocabulary.DISPLAY_PICKINGLIST);

                                    currentFilter = ToolDomainVocabulary.DISPLAY_PICKINGLIST;
                                    //=========MESSAGE 13 SEND===========
                                    myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), new DisplayPickingList(pickingList, ((GeneratedPickingList) predicate).getNextViableInvId())));
                                    myAgent.send(message);
                                    return;
                                    //=========MESSAGE 14.2 RECEIVE===========
                                } else if (predicate instanceof StoredTools) {
                                    HashMap<String, java.util.ArrayList<ToolInstance>> orderedStoredTools = getOrderedStoredToolsMapFromList(((StoredTools) predicate).getToolInstances());
                                    List requestedTools = new ArrayList();
                                    //only add requested stored tools!
                                    Iterator neededToolIterator = toolRequirements.getToolRequirements().iterator();
                                    while (neededToolIterator.hasNext()) {
                                        ToolRequirementEntry neededToolEntry = (ToolRequirementEntry) neededToolIterator.next();
                                        java.util.ArrayList<ToolInstance> storedToolsList = orderedStoredTools.get(neededToolEntry.getTool().getToolId());
                                        if (storedToolsList != null && neededToolEntry.getQuantity() > 0) {
                                            for (ToolInstance storedInstance : storedToolsList) {
                                                if (neededToolEntry.getQuantity() == 0) {
                                                    break;
                                                }
                                                requestedTools.add(storedInstance);
                                                neededToolEntry.setQuantity(neededToolEntry.getQuantity() - 1);
                                            }
                                        }
                                        if (neededToolEntry.getQuantity() > 0) {
                                            logger.warning("Supply Agent " + getAID().getName() + " was sent fewer stored tools than it needed");
                                            ACLMessage message = triggeringMessage.createReply();
                                            message.setPerformative(ACLMessage.FAILURE);
                                            myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(),new ToolInspectionFinished()));
                                            myAgent.send(message);
                                            myAgent.removeBehaviour(this);
                                            return;
                                        }
                                    }

                                    ACLMessage message = new ACLMessage(ACLMessage.QUERY_REF);
                                    message.setOntology(ontology.getName());
                                    message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                    message.addReceiver(dbAgent);
                                    message.setConversationId(conversationId);
                                    message.setProtocol(ToolDomainVocabulary.SETUP_INFORMATION_PROTOCOL);
                                    message.setReplyWith(ToolDomainVocabulary.REQUISITION_TOOLS);

                                    currentFilter = ToolDomainVocabulary.REQUISITION_TOOLS;

                                    myAgent.getContentManager().fillContent(message,
                                            new Action(myAgent.getAID(), new RequisitionTools(requestedTools)));
                                    myAgent.send(message);
                                    return;
                                } else if (predicate instanceof FoundTools) {
                                    List foundTools = ((FoundTools) predicate).getToolInstances();
                                    //=========MESSAGE 15 SEND===========
                                    //add newly assembled tools
                                    Iterator assembledIterator = assembledTools.iterator();
                                    while (assembledIterator.hasNext()) {
                                        foundTools.add(assembledIterator.next());
                                    }

                                    ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                                    message.setOntology(ontology.getName());
                                    message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                    message.addReceiver(visualAgent);
                                    message.setConversationId(conversationId);
                                    message.setProtocol(ToolDomainVocabulary.SETUP_INFORMATION_PROTOCOL);
                                    message.setReplyWith(ToolDomainVocabulary.DISPLAY_COMMISSION);

                                    currentFilter = ToolDomainVocabulary.DISPLAY_COMMISSION;

                                    myAgent.getContentManager().fillContent(message,
                                            new Action(myAgent.getAID(), new DisplayCommissionList(measureDeviation(foundTools))));
                                    myAgent.send(message);
                                    return;
                                }
                                break;
                            case ACLMessage.INFORM:
                                AgentAction action = (AgentAction) ((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                                //=========MESSAGE 14 RECEIVE===========
                                if (action instanceof ToolAssemblyFinished) {
                                    assembledTools = ((ToolAssemblyFinished) action).getToolInstances();
                                    //store tools
                                    storeTools(assembledTools, true);
                                    //add already assembled tools from storage
                                    List toolEntries = toolRequirements.getToolRequirements();
                                    List requiredToolTypes = new ArrayList();
                                    Iterator iterator = toolEntries.iterator();
                                    while (iterator.hasNext()) {
                                        ToolRequirementEntry te = (ToolRequirementEntry) iterator.next();
                                        requiredToolTypes.add(te.getTool());
                                    }
                                    //=========MESSAGE 14.1 SEND===========
                                    ACLMessage message = new ACLMessage(ACLMessage.QUERY_REF);
                                    message.setOntology(ontology.getName());
                                    message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                    message.addReceiver(dbAgent);
                                    message.setConversationId(conversationId);
                                    message.setProtocol(ToolDomainVocabulary.SETUP_INFORMATION_PROTOCOL);
                                    message.setReplyWith(ToolDomainVocabulary.QUERY_STOREDTOOLS);

                                    currentFilter = ToolDomainVocabulary.QUERY_STOREDTOOLS;
                                    
                                    myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), new QueryStoredTools(requiredToolTypes)));
                                    myAgent.send(message);
                                    return;
                                    //=========MESSAGE 16 RECEIVE=========
                                } else if (action instanceof ToolInspectionFinished) {
                                    //=========MESSAGE 16.5 SEND===========
                                    ACLMessage message = triggeringMessage.createReply();
                                    message.setPerformative(ACLMessage.INFORM);
                                    myAgent.getContentManager().fillContent(message, new Action(myAgent.getAID(), action));
                                    myAgent.send(message);
                                    myAgent.removeBehaviour(this);
                                    return;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                } catch (OntologyException | Codec.CodecException | ClassCastException e) {
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Supply Agent " + getAID().getName()
                            + " could not construct a reply to \n" + msg.getContent());
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }

        private HashMap<String, java.util.ArrayList<ToolInstance>> getOrderedStoredToolsMapFromList(jade.util.leap.List storedTools) {
            HashMap<String, java.util.ArrayList<ToolInstance>> orderedToolMap = new HashMap<String, java.util.ArrayList<ToolInstance>>();
            Iterator storedToolsIterator = storedTools.iterator();
            while (storedToolsIterator.hasNext()) {
                ToolInstance storedInstance = (ToolInstance) storedToolsIterator.next();
                java.util.ArrayList<ToolInstance> toolInstanceList;
                if ((toolInstanceList = orderedToolMap.putIfAbsent(storedInstance.getTool().getToolId(), new java.util.ArrayList<ToolInstance>())) == null) {
                    toolInstanceList = orderedToolMap.get(storedInstance.getTool().getToolId());
                }
                if (toolInstanceList.isEmpty()) {
                    toolInstanceList.add(storedInstance);
                }
                for (int i = 0; i < toolInstanceList.size(); i++) {
                    if (toolInstanceList.get(i).getUsedTime() <= storedInstance.getUsedTime()) {
                        toolInstanceList.add(i, storedInstance);
                        break;
                    }
                    //append if the last one is still larger
                    if (i + 1 == toolInstanceList.size()) {
                        toolInstanceList.add(storedInstance);
                        break;
                    }
                }

            }
            return orderedToolMap;
        }
    }

    /**
     * Listens to the result of DB querries made on behalf of other Agent and
     * relays the information
     */
    private class QueryResultListener extends CyclicBehaviour {

        private final String conversationId;
        private final ACLMessage triggeringMessage;

        public QueryResultListener(String conversationId, ACLMessage triggeringMessage) {
            this.conversationId = conversationId;
            this.triggeringMessage = triggeringMessage;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(conversationId));
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                try {
                    ContentElement element = myAgent.getContentManager().extractContent(msg);
                    switch (msg.getPerformative()) {
                        case ACLMessage.NOT_UNDERSTOOD:
                            logger.warning("Supply Agent " + getAID().getName()
                                    + " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                            return;
                        case ACLMessage.INFORM_REF:
                            if (element instanceof StoredTools) {
                                //=========MESSAGE 5 SEND===========
                                StoredTools storedTools = (StoredTools) element;

                                ACLMessage message = triggeringMessage.createReply();
                                message.setPerformative(ACLMessage.INFORM_REF);
                                myAgent.getContentManager().fillContent(message, storedTools);
                                myAgent.send(message);
                                myAgent.removeBehaviour(this);
                                return;
                            }
                            break;
                        default:
                            reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    }
                } catch (OntologyException | Codec.CodecException | ClassCastException e) {
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Supply Agent " + getAID().getName()
                            + " could not construct a reply to \n" + msg.getContent());
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private void storeTools(List toolInstances, boolean justAssembled) throws OntologyException, Codec.CodecException {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology(ontology.getName());
        message.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        message.addReceiver(dbAgent);

        StoreTools storeTools = new StoreTools(toolInstances, justAssembled);
        getContentManager().fillContent(message, new Action(getAID(), storeTools));

        send(message);
    }

    private List measureDeviation(List tools) {
        Iterator toolsIterator = tools.iterator();
        while (toolsIterator.hasNext()) {
            ToolInstance tool = (ToolInstance) toolsIterator.next();
            BigDecimal ld = new BigDecimal(Math.random() - 0.5);
            tool.setLengthDeviation(ld.setScale(3, RoundingMode.FLOOR).floatValue());
            BigDecimal rd = new BigDecimal(Math.random() - 0.5);
            tool.setRadiusDeviation(rd.setScale(3, RoundingMode.FLOOR).floatValue());
        }
        return tools;
    }

    private void lookupAgents() {
        if (dbAgent == null) {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(ToolDomainVocabulary.DATABASE_SERVICE);
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(this, template);
                if (result.length > 0) {
                    dbAgent = result[0].getName();
                } else {
                    logger.severe("DB Agent " + getAID().getName() + " couldn't find a database agent");
                    doDelete();
                }
            } catch (FIPAException fe) {
                logger.severe("DB Agent " + getAID().getName() + " had an exception occurr while searching for the database");
                doDelete();
            }
        }
        if (visualAgent == null) {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(ToolDomainVocabulary.VISUAL_SERVICE);
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(this, template);
                if (result.length > 0) {
                    visualAgent = result[0].getName();
                } else {
                    logger.severe("DB Agent " + getAID().getName() + " couldn't find a visual agent");
                    doDelete();
                }
            } catch (FIPAException fe) {
                logger.severe("DB Agent " + getAID().getName() + " had"
                        + " an exception occurr while searching for visual agents");
                doDelete();
            }
        }
    }
}
