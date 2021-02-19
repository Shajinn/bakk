package testing;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.util.logging.Logger;
import ontology.order.OrderDomainVocabulary;
import ontology.tool.ToolDomainOntology;
import ontology.tool.ToolDomainVocabulary;
import ontology.tool.terms.FoundTools;
import ontology.tool.terms.OrderSetupTime;
import ontology.tool.terms.SetupTimeEntry;
import ontology.tool.terms.StoredTools;
import ontology.tool.terms.ToolInspectionFinished;
import ontology.tool.terms.ToolInstance;
import ontology.tool.terms.ToolSupplyTimes;

/**
 *
 * @author Shahin Mahmody
 */
public class TestingAgent extends GuiAgent {

    private static final Logger logger = Logger.getLogger("MAS");
    private final Codec codec = new SLCodec();
    private final Ontology ontology = ToolDomainOntology.getInstance();

    private AID machineAgent;
    private AID dbAgent;

    private TestScenario scenario = null;

    @Override
    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(OrderDomainVocabulary.PRODUCTION_SERVICE);
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this,
                    template);
            //We assume that there is only one in the model
            if (result.length > 0) {
                machineAgent = result[0].getName();
            } else {
                System.out.println("Tester " + getAID().getName() + " couldn't"
                        + " find any machines");
                doDelete();
            }
        } catch (FIPAException e) {
            System.out.println("Tester " + getAID().getName() + " had"
                    + " an exception occurred while searching for machines");
            doDelete();
        }
        template = new DFAgentDescription();
        sd = new ServiceDescription();
        sd.setType(ToolDomainVocabulary.DATABASE_SERVICE);
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this,
                    template);
            //We assume that there is only one in the model
            if (result.length > 0) {
                dbAgent = result[0].getName();
            } else {
                System.out.println("Tester " + getAID().getName() + " couldn't"
                        + " find any databases");
                doDelete();
            }
        } catch (FIPAException e) {
            System.out.println("Tester " + getAID().getName() + " had"
                    + " an exception occurred while searching for the db");
            doDelete();
        }

        TestInterface ui = new TestInterface(this);
        ui.setSize(500, 800);
        ui.setVisible(true);

    }

    @Override
    protected void onGuiEvent(GuiEvent ge) {
        switch (ge.getType()) {
            case 0:
                scenario = (TestScenario) ge.getParameter(0);
                addBehaviour(new TestSetupper());
                break;
            case 4:
                break;
            default:
                break;
        }
    }

    private class TestSetupper extends CyclicBehaviour {

        private String conversationId;

        @Override
        public void onStart() {
            try {
                if(scenario.dbTools != null){
                    ACLMessage setToolsMsg = new ACLMessage(ACLMessage.REQUEST);
                    setToolsMsg.setOntology(ontology.getName());
                    setToolsMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                    setToolsMsg.setConversationId(conversationId = "TestSetup" + System.currentTimeMillis());
                    setToolsMsg.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                    setToolsMsg.addReceiver(dbAgent);
                    SetTools setTools = new SetTools(scenario.dbTools, true);
                    getContentManager().fillContent(setToolsMsg, new Action(getAID(), setTools));
                    send(setToolsMsg);
                }else{
                    if (scenario.filledTestAction != null) {
                        ACLMessage startTestMsg = new ACLMessage(ACLMessage.REQUEST);
                        startTestMsg.setOntology(ontology.getName());
                        startTestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                        startTestMsg.setConversationId(conversationId = "TestSetup" + System.currentTimeMillis());
                        startTestMsg.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                        startTestMsg.addReceiver(machineAgent);
                        getContentManager().fillContent(startTestMsg, new Action(getAID(), scenario.filledTestAction));
                        send(startTestMsg);
                    }
                }
            } catch (OntologyException | Codec.CodecException e) {
                System.out.println("Tester " + getAID().getName() + " could not construct a message setting up tests");
            }
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
                            logger.warning("Tester " + getAID().getName()
                                    + " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                            return;
                        case ACLMessage.CONFIRM:
                            if (element instanceof StoredTools && msg.getSender().equals(dbAgent)) {
                                ACLMessage setToolsMsg = new ACLMessage(ACLMessage.REQUEST);
                                setToolsMsg.setOntology(ontology.getName());
                                setToolsMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                setToolsMsg.setConversationId(conversationId);
                                setToolsMsg.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                                setToolsMsg.addReceiver(dbAgent);
                                SetTools setTools = new SetTools(scenario.magazineTools, false);
                                getContentManager().fillContent(setToolsMsg, new Action(getAID(), setTools));
                                send(setToolsMsg);
                                return;
                            } else if (element instanceof FoundTools) {
                                ACLMessage setToolsMsg = new ACLMessage(ACLMessage.REQUEST);
                                setToolsMsg.setOntology(ontology.getName());
                                setToolsMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                setToolsMsg.setConversationId(conversationId);
                                setToolsMsg.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                                setToolsMsg.addReceiver(machineAgent);
                                SetTools setTools = new SetTools(scenario.magazineTools);
                                getContentManager().fillContent(setToolsMsg, new Action(getAID(), setTools));
                                send(setToolsMsg);
                                return;
                            } else if (element instanceof StoredTools && msg.getSender().equals(machineAgent)) {
                                ACLMessage setQueueMsg = new ACLMessage(ACLMessage.REQUEST);
                                setQueueMsg.setOntology(ontology.getName());
                                setQueueMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                setQueueMsg.setConversationId(conversationId);
                                setQueueMsg.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                                setQueueMsg.addReceiver(machineAgent);
                                SetMachineQueueEntries setQueue = new SetMachineQueueEntries(scenario.machineQueue);
                                getContentManager().fillContent(setQueueMsg, new Action(getAID(), setQueue));
                                send(setQueueMsg);
                                return;

                            } else if (element instanceof Action && ((Action) element).getAction() instanceof SetMachineQueueEntries) {
                                if (scenario.filledTestAction != null) {
                                    ACLMessage startTestMsg = new ACLMessage(ACLMessage.REQUEST);
                                    startTestMsg.setOntology(ontology.getName());
                                    startTestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                                    startTestMsg.setConversationId(conversationId);
                                    startTestMsg.setProtocol(ToolDomainVocabulary.TEST_PROTOCOL);
                                    startTestMsg.addReceiver(machineAgent);
                                    getContentManager().fillContent(startTestMsg, new Action(getAID(), scenario.filledTestAction));
                                    send(startTestMsg);
                                } else {
                                    TestingResponseWindow ui = new TestingResponseWindow("DONE");
                                    ui.setSize(500, 800);
                                    ui.setVisible(true);
                                    myAgent.removeBehaviour(this);
                                }
                                return;
                            } else if (element instanceof ToolSupplyTimes){
                                StringBuilder sb = new StringBuilder();
                                Iterator setupTimesListsIterator = ((ToolSupplyTimes) element).getSetupTimeLists().iterator();
                                sb.append("Result of Setup Info Request Test\n");
                                if(setupTimesListsIterator.hasNext()){
                                    while(setupTimesListsIterator.hasNext()){
                                            SetupTimeEntry setupTimesEntry = (SetupTimeEntry) setupTimesListsIterator.next();
                                            Iterator setupTimesEntryIterator = setupTimesEntry.getTimesList().iterator();
                                            sb.append("\n\n~~position: ");
                                            sb.append(setupTimesEntry.getPosition());
                                            while(setupTimesEntryIterator.hasNext()){
                                                OrderSetupTime orderSetupTime = (OrderSetupTime) setupTimesEntryIterator.next();
                                                sb.append("\n\nID: "+orderSetupTime.getOrderId());
                                                sb.append("\nTime required: "+orderSetupTime.getTimeRequired());
                                                sb.append("\nStored Tools: "+orderSetupTime.getStoredToolsAmount());
                                                sb.append("\nNew Tools: "+orderSetupTime.getNewToolsAmount());
                                            }
                                    }
                                }else{
                                    sb.append("No lists returned: error case");
                                }
                                TestingResponseWindow ui = new TestingResponseWindow(sb.toString());
                                ui.setSize(500, 800);
                                ui.setVisible(true);
                                myAgent.removeBehaviour(this);
                                return;
                                
                            }else if (element instanceof Action && (((Action) element).getAction() instanceof ToolInspectionFinished)){
                                List finishedTools = ((ToolInspectionFinished) ((Action) element).getAction()).getToolInstances();
                                
                                StringBuilder sb = new StringBuilder();
                                sb.append("Result of Tool Requisition Test \n");
                                if(finishedTools != null && !finishedTools.isEmpty()){
                                    for(Object o: finishedTools.toArray()){
                                        ToolInstance instance = (ToolInstance) o;
                                        sb.append(instance.getTool().getName());
                                        sb.append("\nInstance: ");
                                        sb.append(instance.getInstanceId() );
                                        sb.append("\nUsed Time: ");
                                        sb.append(instance.getUsedTime());
                                        sb.append("\n\n");
                                    }
                                }else{
                                    sb.append("No tools sent or error due to too few tools");
                                }
                                
                                TestingResponseWindow ui = new TestingResponseWindow(sb.toString());
                                ui.setSize(500, 800);
                                ui.setVisible(true);
                                myAgent.removeBehaviour(this);
                                return;
                            }else if (element  instanceof MagazineSetupTestResponse) {
                                MagazineSetupTestResponse mstr = (MagazineSetupTestResponse)element;
                                StringBuilder sb = new StringBuilder();
                                sb.append("Result of Magazine Setup Test \n");
                                if(mstr.getTooLittleLifetime()){
                                    sb.append("Too little lifetime was left on the tools");
                                }else if(mstr.getTooManyTools()){
                                    sb.append("More tools were required for execution than the magazine has space for");
                                }else{
                                    List magazineTools = mstr.getMagazine();
                                    if(magazineTools != null && !magazineTools.isEmpty()){
                                        for(Object o: magazineTools.toArray()){
                                            ToolInstance instance = (ToolInstance) o;
                                            sb.append(instance.getTool().getName());
                                            sb.append("\nInstance: ");
                                            sb.append(instance.getInstanceId() );
                                            sb.append("\nUsed Time: ");
                                            sb.append(instance.getUsedTime());
                                            sb.append("\n\n");
                                        }
                                    }
                                }
                                TestingResponseWindow ui = new TestingResponseWindow(sb.toString());
                                ui.setSize(500, 800);
                                ui.setVisible(true);
                                myAgent.removeBehaviour(this);
                                return;
                            }
                            break;
                        default:
                            break;
                    }
                } catch (OntologyException | Codec.CodecException e) {
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Tester " + getAID().getName()
                            + " could not construct a proper reply to \n" + msg.getContent());
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    public void setScenario(TestScenario scenario) {
        this.scenario = scenario;
    }
}
