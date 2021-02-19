
package agents;

import jade.content.AgentAction;
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
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.util.HashMap;
import java.util.logging.Logger;
import ontology.tool.ToolDomainOntology;
import ontology.tool.ToolDomainVocabulary;
import ontology.tool.terms.DisplayCommissionList;
import ontology.tool.terms.DisplayPickingList;
import ontology.tool.terms.PickingList;
import ontology.tool.terms.PickingListEntry;
import ontology.tool.terms.SetupComplete;
import ontology.tool.terms.StoreTools;
import ontology.tool.terms.TakeOutTools;
import ontology.tool.terms.ToolAssemblyFinished;
import ontology.tool.terms.ToolInspectionFinished;
import ontology.tool.terms.ToolInstance;

/**
 *
 * @author Shahin Mahmody
 */
public class VisualAgent extends GuiAgent{
    private static final Logger logger = Logger.getLogger("MAS");
    private final Codec codec = new SLCodec();
    private final Ontology ontology = ToolDomainOntology.getInstance();
    
    private PickingListGUI pickingListGUI = null;
    private CommissionListGUI commissionListGUI = null;
    private ToolsForStorageGUI toolsForStorageGUI = null;
    
    private AID supplyAgent = null;
    
    @Override
    protected void setup() {
        logger.info("Visual Agent "+ getAID().getName() +" started");
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ToolDomainVocabulary.VISUAL_SERVICE);
        sd.setName(getLocalName()+"-Visual");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            logger.severe("Visual Agent "+getAID().getName()+" failed to register its service");
            doDelete();
        }
        
        addBehaviour(new CommandListener());
    }
    
    @Override
    protected void takeDown() {
        logger.info("Visual Agent "+getAID().getName()+" is shutting down");
        super.takeDown();
    }
    
    private List assembleTools(PickingList pl, int nextInvId){
        ArrayList assembledToolInstances = new ArrayList();
        HashMap<String,java.util.ArrayList<PickingListEntry>> pickingEntryMap = new HashMap<String,java.util.ArrayList<PickingListEntry>>();
        Iterator pickingListIterator = pl.getComponents().iterator();
        while(pickingListIterator.hasNext()){
            PickingListEntry ple = (PickingListEntry) pickingListIterator.next();
            pickingEntryMap.putIfAbsent(ple.getTool().getToolId(),new java.util.ArrayList<PickingListEntry>());
            //get components per tool
            pickingEntryMap.get(ple.getTool().getToolId()).add(ple);
            
        }
        //create tool instances and generate ids
        for(java.util.ArrayList<PickingListEntry> toolParts: pickingEntryMap.values()){
            if(!toolParts.isEmpty()){
                for(int i = 0; i < toolParts.get(0).getQuantity();i++){
                    assembledToolInstances.add(new ToolInstance( 
                            toolParts.get(0).getTool(), Integer.toString(nextInvId), 0, 0, 0));
                    nextInvId++;
                }
            }
        }
        return assembledToolInstances;
    }

    @Override
    protected void onGuiEvent(GuiEvent ge) {
        if(ge.getSource() instanceof PickingListGUI){
            ACLMessage reply = ((ACLMessage)ge.getParameter(0));
            List finishedTools = (List)ge.getParameter(1);
            //=========MESSAGE 14 SEND===========
            try{
                getContentManager().fillContent(reply, new Action(getAID(),new ToolAssemblyFinished(finishedTools)));
                send(reply);
                pickingListGUI.dispose();
            }catch(OntologyException | Codec.CodecException e){
               logger.warning("Visual Agent "+getAID().getName()+" could not construct a proper reply");
            }
        }else if (ge.getSource() instanceof CommissionListGUI){
            //=========MESSAGE 16 SEND===
            ACLMessage reply = (ACLMessage)ge.getParameter(0);
            List toolInstances = (List)ge.getParameter(1);
            try{
                getContentManager().fillContent(reply, new Action(getAID(),new ToolInspectionFinished(toolInstances)));
                send(reply);
                commissionListGUI.dispose();
            }catch(OntologyException | Codec.CodecException e){
               logger.warning("Visual Agent "+getAID().getName()+" could not construct a proper reply");
            }
        }else if (ge.getSource() instanceof ToolsForStorageGUI){               
            //=========MESSAGE 18 SEND===========
            ACLMessage reply = (ACLMessage)ge.getParameter(0);
            List toolInstances = (List)ge.getParameter(1);
            try{
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
                            logger.severe("Visual Agent "+ getAID().getName()+" could"
                                + " not find a supply agent");
                            doDelete();
                        }
                    }catch (FIPAException e) {
                        logger.severe("Visual Agent "+ getAID().getName()+" had"
                                + " an exception occurr while searching for supply agent");
                        doDelete();
                    }
                }
                ACLMessage storeToolsMessage = new ACLMessage(ACLMessage.REQUEST);
                storeToolsMessage.setOntology(ontology.getName());
                storeToolsMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                storeToolsMessage.addReceiver(supplyAgent);
                getContentManager().fillContent(storeToolsMessage, new Action(getAID(),new StoreTools(toolInstances,true)));
                send(storeToolsMessage);
                
                getContentManager().fillContent(reply, new Action(getAID(),new SetupComplete()));
                send(reply);
                toolsForStorageGUI.dispose();
            }catch(OntologyException | Codec.CodecException e){
               logger.warning("Visual Agent "+getAID().getName()+" could not construct a proper reply");
            }
        }else{
            logger.warning("Visual Agent "+getAID().getName()+" got sent an unsupported GUI operation");
            throw new UnsupportedOperationException("Not supported yet."); 
        }
    }
    
    /**
     * Listens for incoming UI commands, providing user interaction
     */
    private class CommandListener extends CyclicBehaviour{

        @Override
        public void action() {
             ACLMessage msg = myAgent.receive();
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                try{
                    switch (msg.getPerformative()) {
                        case ACLMessage.NOT_UNDERSTOOD:
                            logger.warning("Visual Agent "+ getAID().getName()+
                                                    " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                            return;
                        case ACLMessage.REQUEST:
                            AgentAction action  = (AgentAction)((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                            //=========MESSAGE 13 RECEIVE===========
                            if(action instanceof DisplayPickingList){
                                List finishedTools = assembleTools(((DisplayPickingList) action).getPickingList(),
                                        ((DisplayPickingList) action).getNextViableInvId());
                                reply.setPerformative(ACLMessage.INFORM);
                                pickingListGUI = new PickingListGUI((VisualAgent) myAgent, 
                                        finishedTools, ((DisplayPickingList) action).getPickingList().getComponents(),
                                        reply);
                                pickingListGUI.setSize(500, 800);
                                pickingListGUI.setTitle("Tool Assembly");
                                pickingListGUI.setVisible(true);
                                return;
                            //=========MESSAGE 15 RECEIVE===========
                            }else if(action instanceof DisplayCommissionList){
                                DisplayCommissionList dcl = (DisplayCommissionList) action;
                                reply.setPerformative(ACLMessage.INFORM);
                                commissionListGUI = new CommissionListGUI((VisualAgent) myAgent,
                                        dcl.getToolInstances(),reply);
                                commissionListGUI.setSize(500,800);
                                commissionListGUI.setTitle("Commissioned Tools' Deviation Measurements");
                                commissionListGUI.setVisible(true);
                                return;
                            //=========MESSAGE 17 RECEIVE===========
                            }else if(action instanceof TakeOutTools){
                                List takenOutTools = ((TakeOutTools) action).getToolInstances();
                                reply.setPerformative(ACLMessage.INFORM);
                                toolsForStorageGUI = new ToolsForStorageGUI((VisualAgent)myAgent, takenOutTools, reply);
                                toolsForStorageGUI.setSize(500,800);
                                toolsForStorageGUI.setTitle("Tools going back to storage");
                                toolsForStorageGUI.setVisible(true);
                                return;
                            }
                            break;
                        case ACLMessage.CONFIRM:
                            //no action required
                            return;
                        default:
                            break;
                    }
                }catch(OntologyException | Codec.CodecException e){
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("Visual Agent "+ getAID().getName()+
                                                    " could not construct a proper reply to \n"+msg.getContent());
                }
                myAgent.send(reply);
            }else{
                block();
            }
        }
    }
}
