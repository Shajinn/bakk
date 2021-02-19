
package agents;

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import ontology.tool.ToolDomainOntology;
import ontology.tool.ToolDomainVocabulary;
import ontology.tool.terms.Component;
import ontology.tool.terms.NcProgramReferencesSheet;
import ontology.tool.terms.PickingList;
import ontology.tool.terms.GeneratedPickingList;
import ontology.tool.terms.DeleteTools;
import ontology.tool.terms.FoundTools;
import ontology.tool.terms.PickingListEntry;
import ontology.tool.terms.QueryComponentsForDynamicSettingSheet;
import ontology.tool.terms.QuerySettingSheet;
import ontology.tool.terms.QueryStoredTools;
import ontology.tool.terms.RequisitionTools;
import ontology.tool.terms.SettingSheet;
import ontology.tool.terms.StoreTools;
import ontology.tool.terms.StoredTools;
import ontology.tool.terms.Tool;
import ontology.tool.terms.ToolInstance;
import ontology.tool.terms.ToolRequirementEntry;
import ontology.tool.terms.ToolRequirementList;
import testing.SetTools;

/**
 * The database representation of tool storage knowledge.
 * This agent stays alive perpetually, serving querries
 * 
 * @author Shahin Mahmody
 */
public class DBAgent extends Agent{
    private static final Logger logger = Logger.getLogger("MAS");
    
    private static final String GET_SETTINGSHEET_BY_NCPROGRAMNR = "select objdata.objid, objdata.ObjTxt as 'Name',  a.ValStr as 'NcProgramNr', b.ValNum as'SetupTime', c.ValNum as 'CycleTime' \n" +
                                                                    "from valdata a inner join valdata b on a.objid =b.objid inner join valdata c  on a.objid = c.objid inner join objdata on a.objid = objdata.objid \n" +
                                                                    "where a.ValStr = ? and a.fieldid = 254 and  b.fieldid = 12270 and c.fieldid = 1256 and a.objid in (select objid from grpref where GrpObjId = 8722) ";
    private static final String GET_SETTINGSHEET_TOOLS_BY_NCPROGRAMNR = "select objdata.objid as 'Id', objdata.DescrTxt as 'Name',  a.ValNum as 'Lifetime', b.ValNum as 'CriticalTime', c.ValText as 'UseTime', d.valNum as 'TemplateLifetime'\n" +
                                                                        "from valdata a inner join  valinvdata b  on a.objid = b.objid inner join objdata on a.objid = objdata.objid \n" +
                                                                        "inner join valinvdata c on b.ObjInv = c.objinv inner join valinvdata d on c.objinv = d.objinv\n" +
                                                                        "where a.objid in (select objid from grpref where GrpObjId = 8721) and a.fieldid = 166 and b.fieldid = 168 and c.fieldid = 49 and  d.fieldid = 166 and c.ObjInv in\n" +
                                                                        "	(select refobjinv from objrefdata \n" +
                                                                        "		where objid in \n" +
                                                                        "			(SELECT objid from  valdata \n" +
                                                                        "			where fieldid = 254 and valdata.ObjId in (select objid from grpref where GrpObjId = 8722) and valStr = ?) \n" +
                                                                        "		and refobjType = 7)";
    private static final String GET_COMPONENTS_FOR_TOOLS = "select distinct tool_compList.Objid as 'ToolType', compList_comp.RefObjId as 'Id', objdata.objtxt as 'Name', objdata.DescrTxt as 'Description' \n" +
                                                            "from objrefdata tool_compList inner join objrefdata compList_comp on tool_compList.RefObjId = compList_comp.ObjId inner join objdata on objdata.ObjId = compList_comp.RefObjId\n" +
                                                            "where tool_compList.RefObjType = 14 and compList_comp.RefObjType = 11 and tool_compList.Objid in (";
    private static final String GET_ALL_TOOLINSTANCES = "select objdata.objid as 'ToolId', d.ObjInv as 'InstanceId', objdata.DescrTxt as 'Name',  a.ValNum as 'Lifetime', b.ValNum as'RemainingLifetime', c.ValNum as 'CriticalTime', d.ValText as 'UseTime'\n" +
                                                            "from valdata a inner join valinvdata b on a.objid =b.objid \n" +
                                                            "	inner join valdata c  on a.objid = c.objid \n" +
                                                            "	inner join objdata on a.objid = objdata.objid \n" +
                                                            "	inner join valinvdata d on b.ObjInv = d.objinv\n" +
                                                            "where a.fieldid = 166 and  b.fieldid = 167 and c.fieldid = 168 and d.fieldid = 49";
    
    private static final String GET_ALL_TOOLS = "select objdata.objid as 'Id', c.ObjInv, objdata.DescrTxt as 'Name',  a.ValNum as 'Lifetime', b.ValNum as 'CriticalTime', c.ValText as 'UseTime', d.valNum as 'TemplateLifetime'\n" +
                                                    "from valdata a inner join  valinvdata b  on a.objid = b.objid inner join objdata on a.objid = objdata.objid \n" +
                                                    "	inner join valinvdata c on b.ObjInv = c.objinv inner join valinvdata d on c.objinv = d.objinv\n" +
                                                    "where a.objid in (select objid from grpref where GrpObjId = 8721) and a.fieldid = 166 and b.fieldid = 168 and c.fieldid = 49 and  d.fieldid = 166 and c.ObjInv in\n" +
                                                    "	(select refobjinv from objrefdata \n" +
                                                    "		where objid in \n" +
                                                    "			(SELECT objid from  valdata \n" +
                                                    "			where fieldid = 254 and valdata.ObjId in (select objid from grpref where GrpObjId = 8722)) \n" +
                                                    "		and refobjType = 7) ";
    
   private static final String GET_MAX_INVID = "select MAX(objinv) as 'MaxInvId' \n" +
                                                "from valinvdata \n" +
                                                "where tableid = 9";
    
    private final Codec codec = new SLCodec();
    private final Ontology ontology = ToolDomainOntology.getInstance();
    
    private Connection dbConnection = null;
    private File logFile = null;
    private Handler fileHandler = null;
    
    private String dbName;
    private String username;
    private String password;
    private String host;
    private int port = 0;
    
    //tools currently in storage
    private HashMap<String, ToolInstance> storedTools;
    //tools that are anywhere but in storage
    private HashMap<String, ToolInstance> toolsInUse;
    //the different types of tools that exist in the db
    private HashMap<String, Tool> toolTypes;
    //highest inv. id currently in the db (as far as we know)
    private int maxInvId;
    
    @Override
    protected void setup() {
       
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
    
         //get arguments
        Object[] args = getArguments();
        if (args != null && args.length >= 5) {           
            dbName = args[0].toString();
            username = args[1].toString();
            password = args[2].toString();
            host = args[3].toString();
            try{
                port = Integer.parseInt(args[4].toString());
            }catch(NumberFormatException e){
                System.err.println("DB Agent "+getAID().getName()+" got an invalid port number");
                doDelete();
            }
            if(args.length >= 7){
               String logLocation = args[5].toString();
               String logLevel = args[6].toString();

               //set up logger
               try{
                   Date now = new Date();
                   DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss");
                   logFile = new File(logLocation+formatter.format(now)+".log");
                   if(!logFile.exists()){
                       logFile.getParentFile().mkdirs();
                       logFile.createNewFile();
                   }
                   fileHandler = new FileHandler(logFile.getName());
                   fileHandler.setFormatter(new SimpleFormatter(){
                       private static final String format = "%3$s %n";

                        @Override
                        public synchronized String format(LogRecord lr) {
                            return String.format(format,
                                    "",
                                    "",
                                    lr.getMessage()
                            );
                        }
                   });
                   logger.addHandler(fileHandler);
                   logger.setLevel(Level.parse(logLevel));

               }catch(IOException e){
                   System.err.println("Failed trying to write to log file");
               }catch(IllegalArgumentException e){
                   System.err.println("LogLevel invalid");
               }
            }else{
                logger.setLevel(Level.OFF);
            }
        }else{
            System.err.println("DB Agent started with insufficient arguments. Requires at least: dbName username password host port");
            doDelete();
        }
        
        logger.info("DB Agent "+ getAID().getName() +" started");
        
        //register service
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ToolDomainVocabulary.DATABASE_SERVICE);
        sd.setName(getLocalName()+"-DB");
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            logger.severe("DB Agent "+getAID().getName()+" failed to register its service");
            doDelete();
        }
        try{
            connectToDB();
        }catch(SQLException | ClassNotFoundException e){
            logger.severe("DB Agent "+getAID().getName()+" couldn't connect to the database");
            doDelete();
        } 
        //make a copy of: 
        storedTools = new HashMap<String,ToolInstance>();
        toolsInUse = new HashMap<String,ToolInstance>();
        toolTypes = new HashMap<String, Tool>();
        
        //Tool Types
        PreparedStatement toolsStatement = null;
        try{
            toolsStatement = getDbConnection().prepareStatement(GET_ALL_TOOLS);
            ResultSet ssToolsResultSet = toolsStatement.executeQuery();
            while(ssToolsResultSet.next()){
                Tool t = new Tool(ssToolsResultSet.getString("Id"),ssToolsResultSet.getFloat("Lifetime"), ssToolsResultSet.getFloat("CriticalTime"), ssToolsResultSet.getString("Name"));
                toolTypes.put(t.getToolId(), t);
            }
        }catch(SQLException e){
            logger.severe("DB Agent "+getAID().getName()+" could not get tool types from the DB");
            doDelete();
        }finally{
            if(toolsStatement != null){
                try{
                    toolsStatement.close();
                }catch(SQLException e){
                    logger.warning("DB Agent "+getAID().getName()+" failed to close a prepared statement");
                }
            }
        }
        
        //Tool Instances
        PreparedStatement toolInstancesStatement = null;
        try{
            toolInstancesStatement = getDbConnection().prepareStatement(GET_ALL_TOOLINSTANCES);
            ResultSet toolInstancesResult = toolInstancesStatement.executeQuery();
            while(toolInstancesResult.next()){
                Tool t = toolTypes.get(toolInstancesResult.getString("ToolId"));
                String instanceId = toolInstancesResult.getString("InstanceId");
                if(t != null && instanceId != null && !instanceId.isEmpty()){
                    ToolInstance ti = new ToolInstance(t,instanceId,
                            BigDecimal.valueOf(t.getLifetime() - toolInstancesResult.getFloat("RemainingLifetime")).setScale(3,RoundingMode.HALF_UP).floatValue(),0,0);
                    storedTools.put(ti.getInstanceId(),ti);
                }
            }
        }catch(SQLException e){
            logger.severe("DB Agent "+getAID().getName()+" could not get tool instances DB");
            doDelete();
        }finally{
            if(toolInstancesStatement != null){
                try{
                    toolInstancesStatement.close();
                }catch(SQLException e){
                    logger.warning("DB Agent "+getAID().getName()+" failed to close a prepared statement");
                }
            }
        }
        
         //Max Inv. Id
        PreparedStatement maxIdStatement = null;
        try{
            maxIdStatement = getDbConnection().prepareStatement(GET_MAX_INVID);
            ResultSet maxIdResult = maxIdStatement.executeQuery();
            if(maxIdResult.next()){
                maxInvId = Integer.parseInt(maxIdResult.getString("MaxInvId"));
            }else{
                maxInvId = 0;
            }
        }catch(SQLException | NumberFormatException e){
            logger.severe("DB Agent "+getAID().getName()+" could not get the currently highest inv. id from the DB");
            doDelete();
        }finally{
            if(maxIdStatement != null){
                try{
                    maxIdStatement.close();
                }catch(SQLException e){
                    logger.warning("DB Agent "+getAID().getName()+" failed to close a prepared statement");
                }
            }
        }
        
        addBehaviour(new QueryListener());
        addBehaviour(new TestListener());
    }
    
    @Override
    protected void takeDown() {
        if(dbConnection != null){
            try{
                dbConnection.close();
            }catch(SQLException e){
                logger.warning("DB Agent "+ getAID().getName()+" could not close the DB connection");
            }
        }
        if(fileHandler != null){
            fileHandler.close();
        }
        System.out.println("DB Agent "+ getAID().getName() + " is shutting down");
        logger.info("DB Agent "+ getAID().getName()+" is shutting down");
        super.takeDown(); 
    }
    
    /**
     * Get a setting sheet from the database, given an NC program identifier
     * @param ncProgramNr the NC program identifier
     * @return a filled in setting sheet
     * @throws SQLException 
     */
    private SettingSheet getSettingSheet(String ncProgramNr) throws SQLException{
        SettingSheet ss = null;
        PreparedStatement ssMetadataStatement = null;
        PreparedStatement ssToolsStatement = null;        
        try{
            //Get SettingSheet Data first
            ssMetadataStatement = getDbConnection().prepareStatement(GET_SETTINGSHEET_BY_NCPROGRAMNR);
            ssMetadataStatement.setString(1, ncProgramNr);
            ResultSet ssMetadataResultSet = ssMetadataStatement.executeQuery();
            if(ssMetadataResultSet.next()){
                ss = new SettingSheet(ssMetadataResultSet.getString("Name"), ncProgramNr, ssMetadataResultSet.getFloat("CycleTime"),ssMetadataResultSet.getFloat("SetupTime"), null);
                //Then get tools
                ssToolsStatement = getDbConnection().prepareStatement(GET_SETTINGSHEET_TOOLS_BY_NCPROGRAMNR);
                ssToolsStatement.setString(1, ncProgramNr);
                ResultSet ssToolsResultSet = ssToolsStatement.executeQuery();
                List toolList = new ArrayList();
                while(ssToolsResultSet.next()){
                    Tool t = new Tool(ssToolsResultSet.getString("Id"),ssToolsResultSet.getFloat("Lifetime"), ssToolsResultSet.getFloat("CriticalTime"), ssToolsResultSet.getString("Name"));
                    toolList.add(new ToolRequirementEntry(t, extractUseTimeFromDBText(ssToolsResultSet.getString("UseTime")),0,ssToolsResultSet.getFloat("TemplateLifetime")));
                }
                ss.setToolRequirementsList(new ToolRequirementList(toolList, ToolRequirementList.TYPE_LIFETIME));
            }
        }finally{
            if(ssMetadataStatement != null){
                ssMetadataStatement.close();
            }
            if(ssToolsStatement != null){
                ssToolsStatement.close();
            }
        }
        return ss;
    }
    
    /**
     * Exract the necessary usetime from the complex string returned from the database
     * @param text the database string
     * @return a float value, extracted from the database string
     */
    private float extractUseTimeFromDBText(String text){
        float useTime = -1;
        try{
            useTime = Float.parseFloat(text.substring(112, text.length()-9).replace(',', '.'));
        }catch(NumberFormatException e){
            logger.severe("Order Agent "+ getAID().getName()+" could not extract usetime from a db entry");
        }
        return useTime; 
    }
    
    
    /**
     * Create a picking list of necessary components per tool, given info from the database
     * @param toolRequirements describes how many new tools of various types are 
     * regarded when choosing the components for the picking list
     * @return a picking list, filled with components to construct various tools
     * @throws SQLException 
     */
     private PickingList getPickingList(ToolRequirementList toolRequirements) throws SQLException{
        PickingList pickingList = new PickingList(new ArrayList());
        HashMap<String, ToolRequirementEntry> tempRequirementsMap = new HashMap<String, ToolRequirementEntry>();
        PreparedStatement pickingListStatement = null;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < toolRequirements.getToolRequirements().size(); i++){
            sb.append("?,");
        }
        sb.replace(sb.length()-1, sb.length(), ")");
        try{
            pickingListStatement = getDbConnection().prepareStatement(GET_COMPONENTS_FOR_TOOLS+sb.toString());
            Iterator iterator = toolRequirements.getToolRequirements().iterator();
            int counter = 1;
            while(iterator.hasNext()){
                ToolRequirementEntry tre = ((ToolRequirementEntry)iterator.next());
                pickingListStatement.setString(counter, tre.getTool().getToolId());
                tempRequirementsMap.put(tre.getTool().getToolId(),tre);
                counter++;
            }
            ResultSet pickingListResult = pickingListStatement.executeQuery();
            while(pickingListResult.next()){
                Component component = new Component(pickingListResult.getString("Id"), pickingListResult.getString("Name"),
                        pickingListResult.getString("Description"), pickingListResult.getString("ToolType"));
                ToolRequirementEntry tre = tempRequirementsMap.get(component.getToolId());
                pickingList.getComponents().add(new PickingListEntry(component,tre.getQuantity(),tre.getTool()));
            }
        }finally{
            if(pickingListStatement != null){
                pickingListStatement.close();
            }
        }
        return pickingList;
    }
    
     /**
      * Return stored tool instances, given a collection of tools
      * @param tools tool type ids and their associated tool info
      * @return a list of all found tools
      * @throws SQLException 
      */
    private List getToolInstancesForTypes(HashMap<String,Tool> tools) throws SQLException{
        List toolInstances = new ArrayList();
        if(tools != null){
            for(ToolInstance instance: storedTools.values()){
                toolInstances.add(instance);
            }
        }
        return toolInstances;
    }
    
    /**
     * Move tools from the 'in use' list to the 'stored' list, or just put  
     * newly arrived tools into the 'stored' list. Also updates maxInvId if the
     * tools are new
     * @param toolInstances tools to be stored
     * @param justAssembled are the incoming tools new?
     */
    private void storeToolInstances(List toolInstances,boolean justAssembled){
        if(toolInstances != null){
            Iterator toolIterator = toolInstances.iterator();
            while(toolIterator.hasNext()){
                //Store or update
                ToolInstance toolInstance = (ToolInstance) toolIterator.next();
                if(justAssembled){
                    toolsInUse.put(toolInstance.getInstanceId(),toolInstance);
                    try{
                        int parsedId = Integer.parseInt(toolInstance.getInstanceId());
                        if(parsedId > maxInvId){
                            maxInvId = parsedId; 
                        }
                    }catch(NumberFormatException e){
                        //Custom non-number id, not actuall an issue
                    }
                }else{
                    storedTools.put(toolInstance.getInstanceId(), toolInstance);
                    //take out of the "in use" list
                    toolsInUse.remove(toolInstance.getInstanceId());
                }
            }
        }
    }
    
    /**
     * Remove tools from the system
     * @param toolInstances the tools to be removed
     * @return
     */
    private void deleteDestroyedTools(List toolInstances){
        if(toolInstances != null){
            Iterator toolIterator = toolInstances.iterator();
            while(toolIterator.hasNext()){
                ToolInstance toolInstance = (ToolInstance) toolIterator.next();
                toolsInUse.remove(toolInstance.getInstanceId());
            }
        }
    }
    
    /**
     * Move tools from the 'stored' list to the 'in use' list
     * @param toolInstances the tools to be moved
     * @return a list of all tools that have been moved
     */
    private List takeToolsOutOfStorage(List toolInstances){
        List foundTools = new ArrayList();
        if(toolInstances != null){
            Iterator toolIterator = toolInstances.iterator();
            while(toolIterator.hasNext()){
                ToolInstance toolInstance = (ToolInstance) toolIterator.next();
                //removed from storage list
                toolInstance = storedTools.remove(toolInstance.getInstanceId());
                if(toolInstance != null){
                    //put into "in use" list
                    toolsInUse.put(toolInstance.getInstanceId(),toolInstance);
                    foundTools.add(toolInstance);
                }
            }
        }
        return foundTools;
    } 
    
    private Connection getDbConnection() {
        try{
            if(dbConnection == null || dbConnection.isClosed()){
                connectToDB();
            }
            return dbConnection;
        }catch(SQLException | ClassNotFoundException e){
            System.out.println("Shutting down Agent " + getName() + ": Could not connect to database");
            logger.severe("DB Agent "+getAID().getName()+" couldn't connect to the database");
            doDelete();
            return null;
        }
    }
    
    private void connectToDB() throws SQLException, ClassNotFoundException{
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String url = "jdbc:sqlserver://"+host;
        if(port > 0){
            url += ":" + port;
        }
        url += ";databaseName="+dbName;
        dbConnection = DriverManager.getConnection(url, username, password);
        System.out.println(dbConnection.getMetaData().getDatabaseProductName());
    }
    
    /**
     * Listens for storage querries and responds
     */
    private class QueryListener extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(MessageTemplate.not(MessageTemplate.MatchProtocol(ToolDomainVocabulary.TEST_PROTOCOL)));
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent(msg.getContent());
                try{
                    AgentAction action  = (AgentAction)((Action) myAgent.getContentManager().extractContent(msg)).getAction();
                    switch (msg.getPerformative()){
                        case ACLMessage.NOT_UNDERSTOOD:
                            logger.warning("DB Agent "+ getAID().getName()+
                                                    " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                            return;
                        case ACLMessage.QUERY_REF:
                            //=========MESSAGE 11 RECEIVE===========  
                            if(action instanceof QueryComponentsForDynamicSettingSheet){
                                System.out.println("DB Agent "+getAID().getName()+" received component query for "+((QueryComponentsForDynamicSettingSheet) action).getToolRequirementsList().getToolRequirements().size() + " tool types ");
                                ToolRequirementList toolRequirements = ((QueryComponentsForDynamicSettingSheet) action).getToolRequirementsList();
                                reply.setPerformative(ACLMessage.INFORM_REF);
                                //=========MESSAGE 12 SEND===========  
                                myAgent.getContentManager().fillContent(reply, new GeneratedPickingList(getPickingList(toolRequirements),++maxInvId));
                                System.out.println("DB Agent "+getAID().getName()+" sent component info back");
                            //=========MESSAGE 2 RECEIVE===========   
                            }else if(action instanceof QuerySettingSheet){
                                String ncProgramNr = ((QuerySettingSheet) action).getNcProgramNr();
                                System.out.println("DB Agent "+getAID().getName()+" received query for nc program "+ncProgramNr);
                                reply.setPerformative(ACLMessage.INFORM_REF);
                                //=========MESSAGE 3 SEND===========
                                myAgent.getContentManager().fillContent(reply, new NcProgramReferencesSheet(ncProgramNr, getSettingSheet(ncProgramNr)));
                                System.out.println("DB Agent "+getAID().getName()+" sent info for nc program "+ncProgramNr);
                            }else if(action instanceof QueryStoredTools){
                                reply.setPerformative(ACLMessage.INFORM_REF);
                                Iterator i = ((QueryStoredTools) action).getTools().iterator();
                                System.out.println("DB Agent "+getAID().getName()+" received query for "+((QueryStoredTools) action).getTools().size() + " tool types ");
                                HashMap<String,Tool> toolMap = new HashMap<String,Tool>();
                                while(i.hasNext()){
                                    Tool t = (Tool) i.next();
                                    toolMap.putIfAbsent(t.getToolId(), t);
                                }
                                myAgent.getContentManager().fillContent(reply, new StoredTools(getToolInstancesForTypes(toolMap)));
                                System.out.println("DB Agent "+getAID().getName()+" sent info on stored tools ");
                            } else if(action instanceof RequisitionTools){
                                RequisitionTools reqTools = (RequisitionTools)action;
                                reply.setPerformative(ACLMessage.INFORM_REF);
                                myAgent.getContentManager().fillContent(reply,  new FoundTools(takeToolsOutOfStorage(reqTools.getToolInstances())));
                            }
                            break;
                        case ACLMessage.REQUEST:
                            if(action instanceof StoreTools){
                                StoreTools storeTools = ((StoreTools) action);
                                storeToolInstances(storeTools.getToolInstances(),storeTools.getJustAssembled());
                                reply.setPerformative(ACLMessage.CONFIRM);
                            }
                            break;
                        case ACLMessage.INFORM:
                            //=========MESSAGE 19 RECEIVE===========  
                            if(action instanceof DeleteTools){
                                deleteDestroyedTools(((DeleteTools) action).getToolInstances());
                                reply.setPerformative(ACLMessage.CONFIRM);
                            }
                            break;
                        default:
                            break;
                    }
                }catch (OntologyException | Codec.CodecException | ClassCastException e) {
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("DB Agent "+ getAID().getName()+
                                                    " could not construct a proper reply to \n"+msg.getContent());
                }catch (SQLException e){
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.severe("Order Agent "+ getAID().getName()+
                                                    " had an error occur trying to query the database for another agent");
                }
                myAgent.send(reply);
            }else{
                block();
            }
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
                    ContentElement element = myAgent.getContentManager().extractContent(msg);     
                    switch (msg.getPerformative()){
                        case ACLMessage.NOT_UNDERSTOOD:
                            logger.warning("DB Agent "+ getAID().getName()+
                                                    " got a NOT_UNDERSTOOD message from " + msg.getSender().getName());
                            return;
                        case ACLMessage.REQUEST:
                            AgentAction action = (AgentAction)((Action) element).getAction();
                            //set stored tools
                            if(action instanceof SetTools){
                                SetTools tools = ((SetTools) action);
                                if(tools.getStored()){
                                    storedTools.clear();
                                }else{
                                    toolsInUse.clear();
                                }
                                if(tools.getToolInstances() == null){
                                    tools.setToolInstances(new ArrayList());
                                }
                                Iterator toolIterator = tools.getToolInstances().iterator();
                                while(toolIterator.hasNext()){
                                    ToolInstance tool = (ToolInstance) toolIterator.next();
                                    try{
                                        int parsedId = Integer.parseInt(tool.getInstanceId());
                                        if(parsedId > maxInvId){
                                            maxInvId = parsedId;
                                        }
                                    }catch(NumberFormatException e){
                                    }
                                    if(tools.getStored()){
                                        storedTools.put(tool.getInstanceId(), tool);
                                    }else{
                                        toolsInUse.put(tool.getInstanceId(), tool);
                                    }
                                }
                                if(tools.getStored()){
                                    getContentManager().fillContent(reply, new StoredTools());
                                }else{
                                    getContentManager().fillContent(reply, new FoundTools());
                                }
                            }
                            reply.setPerformative(ACLMessage.CONFIRM);
                            break;
                        default:
                            break;
                            
                    }
                }catch (OntologyException | Codec.CodecException | ClassCastException e) {
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    logger.warning("DB Agent "+ getAID().getName()+
                                                    " could not construct a proper reply to \n"+msg.getContent());
                }
                myAgent.send(reply);
            }else{
                block();
            }
        }
    }
}
