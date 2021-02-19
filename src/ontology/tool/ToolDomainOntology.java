
package ontology.tool;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;
import java.math.BigDecimal;
import java.util.logging.Logger;
import ontology.tool.terms.*;
import testing.*;

/**
 * The Ontology class for Agents that handle Tools
 *
 * @author Shahin Mahmody
 */
public class ToolDomainOntology extends Ontology implements ToolDomainVocabulary{
    public static final String ONTOLOGY_NAME = "toolDomainOntology";
    private static final Ontology instance = new ToolDomainOntology();
    private static final Logger logger = Logger.getLogger("MAS");
    
    public static final int FLOAT_SCALE = 100;
    public static final BigDecimal FLOAT_SCALE_MULTIPLIER = new BigDecimal(FLOAT_SCALE);
    
    private ToolDomainOntology(){
        super(ONTOLOGY_NAME, BasicOntology.getInstance());
        
        try{
            //~~~~~~~~~~~~~~~~~~~~~~~CONCEPTS~~~~~~~~~~~~~~~~~~~~~~~~~~
            //Tool
            add(new ConceptSchema(TOOL),Tool.class);
            ConceptSchema cs = (ConceptSchema) getSchema(TOOL);
            cs.add(TOOL_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(TOOL_LIFETIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(TOOL_CRITICALTIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(TOOL_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            
            //Component
            add(new ConceptSchema(COMPONENT),Component.class);
            cs = (ConceptSchema) getSchema(COMPONENT);
            cs.add(COMPONENT_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(COMPONENT_TOOL, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(COMPONENT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(COMPONENT_DESCRIPTION, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            
            //Tool Instance
            add(new ConceptSchema(TOOLINSTANCE),ToolInstance.class);
            cs = (ConceptSchema) getSchema(TOOLINSTANCE);
            cs.add(TOOLINSTANCE_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(TOOLINSTANCE_TOOL, (ConceptSchema) getSchema(TOOL));
            cs.add(TOOLINSTANCE_USEDTIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(TOOLINSTANCE_LENGTHDEVIATION, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(TOOLINSTANCE_RADIUSDEVIATION, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            
            //Tool List Entry
            add(new ConceptSchema(TOOLREQUIREMENTENTRY),ToolRequirementEntry.class);
            cs = (ConceptSchema) getSchema(TOOLREQUIREMENTENTRY);
            cs.add(TOOLREQUIREMENTENTRY_QUANTITY, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            cs.add(TOOLREQUIREMENTENTRY_USETIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(TOOLREQUIREMENTENTRY_TEMPLATETOOLLIFE, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(TOOLREQUIREMENTENTRY_TOOL, (ConceptSchema) getSchema(TOOL));
            
            //Tool requirements list
            add(new ConceptSchema(TOOLREQUIREMENTSLIST),ToolRequirementList.class);
            cs = (ConceptSchema) getSchema(TOOLREQUIREMENTSLIST);
            cs.add(TOOLREQUIREMENTSLIST_REQUIREMENTTYPE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            cs.add(TOOLREQUIREMENTSLIST_TOOLREQUIREMENTS, (ConceptSchema) getSchema(TOOLREQUIREMENTENTRY), 0, ObjectSchema.UNLIMITED);
            
            //Setting Sheet
            add(new ConceptSchema(SETTINGSHEET),SettingSheet.class);
            cs = (ConceptSchema) getSchema(SETTINGSHEET);
            cs.add(SETTINGSHEET_TOOLREQUIREMENTSLIST, (ConceptSchema) getSchema(TOOLREQUIREMENTSLIST));
            cs.add(SETTINGSHEET_NCPROGRAMNR, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(SETTINGSHEET_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(SETTINGSHEET_THROUGHPUTTIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(SETTINGSHEET_SETUPTIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            
            //Picking List Entry
            add(new ConceptSchema(PICKINGLISTENTRY),PickingListEntry.class);
            cs = (ConceptSchema) getSchema(PICKINGLISTENTRY);
            cs.add(PICKINGLISTENTRY_COMPONENT, (ConceptSchema) getSchema(COMPONENT));
            cs.add(PICKINGLISTENTRY_TOOL, (ConceptSchema) getSchema(TOOL));
            cs.add(PICKINGLISTENTRY_QUANTITY, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            
            //Picking List
            add(new ConceptSchema(PICKINGLIST),PickingList.class);
            cs = (ConceptSchema) getSchema(PICKINGLIST);
            cs.add(PICKINGLIST_COMPONENTS, (ConceptSchema) getSchema(PICKINGLISTENTRY), 0, ObjectSchema.UNLIMITED);
            
            add(new ConceptSchema(USETIMELISTENTRY),UsetimeListEntry.class);
            cs = (ConceptSchema) getSchema(USETIMELISTENTRY);
            cs.add(USETIMELISTENTRY_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(USETIMELISTENTRY_TOOLREQUIREMENTLIST, (ConceptSchema) getSchema(TOOLREQUIREMENTSLIST));
            cs.add(USETIMELISTENTRY_MACHININGTIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(USETIMELISTENTRY_PREVIOUSORDERMACHININGTIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            
            add(new ConceptSchema(USETIMELIST),UsetimeList.class);
            cs = (ConceptSchema) getSchema(USETIMELIST);
            cs.add(USETIMELIST_TOOLUSAGES, (ConceptSchema) getSchema(USETIMELISTENTRY), 0, ObjectSchema.UNLIMITED);
            cs.add(USETIMELIST_POSITION, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            
            
            //~~~~~~~~~~~~~~~~~~~~~~~ACTIONS~~~~~~~~~~~~~~~~~~~~~~~~~~
            add(new AgentActionSchema(QUERY_SETTINGSHEET),QuerySettingSheet.class);
            AgentActionSchema aas = (AgentActionSchema) getSchema(QUERY_SETTINGSHEET);
            aas.add(QUERY_SETTINGSHEET_NCPROGRAMNR, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            
            add(new AgentActionSchema(REQUEST_TOOLSUPPLY_TIME),RequestToolSupplyTime.class);
            aas = (AgentActionSchema) getSchema(REQUEST_TOOLSUPPLY_TIME);
            aas.add(REQUEST_TOOLSUPPLY_TIME_USETIMELISTS, (ConceptSchema) getSchema(USETIMELIST), 0, ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(REQUISITION_TOOLS),RequisitionTools.class);
            aas = (AgentActionSchema) getSchema(REQUISITION_TOOLS);
            aas.add(REQUISITION_TOOLS_TOOLINSTANCES, (ConceptSchema) getSchema(TOOLINSTANCE),0,ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(QUERY_COMPONENTS_FOR_DYNAMIC_SETTINGSHEET),QueryComponentsForDynamicSettingSheet.class);
            aas = (AgentActionSchema) getSchema(QUERY_COMPONENTS_FOR_DYNAMIC_SETTINGSHEET);
            aas.add(QUERY_COMPONENTS_FOR_DYNAMIC_SETTINGSHEET_TOOLREQUIREMENTS, (ConceptSchema) getSchema(TOOLREQUIREMENTSLIST));
            
            add(new AgentActionSchema(QUERY_STOREDTOOLS),QueryStoredTools.class);
            aas = (AgentActionSchema) getSchema(QUERY_STOREDTOOLS);
            aas.add(QUERY_STOREDTOOLS_TOOLS, (ConceptSchema) getSchema(TOOL), 0, ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(DISPLAY_PICKINGLIST),DisplayPickingList.class);
            aas = (AgentActionSchema) getSchema(DISPLAY_PICKINGLIST);
            aas.add(DISPLAY_PICKINGLIST_PICKINGLIST, (ConceptSchema) getSchema(PICKINGLIST));
            aas.add(DISPLAY_PICKINGLIST_NEXTVIABLEINVID, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            
            add(new AgentActionSchema(STORE_TOOLS),StoreTools.class);
            aas = (AgentActionSchema) getSchema(STORE_TOOLS);
            aas.add(STORE_TOOLS_INSTANCES, (ConceptSchema) getSchema(TOOLINSTANCE), 0, ObjectSchema.UNLIMITED);
            aas.add(STORE_TOOLS_JUSTASSEMBLED, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
            
            add(new AgentActionSchema(TOOLASSEMBLY_FINISHED),ToolAssemblyFinished.class);
            aas = (AgentActionSchema) getSchema(TOOLASSEMBLY_FINISHED);
            aas.add(TOOLASSEMBLY_FINISHED_TOOLS, (ConceptSchema) getSchema(TOOLINSTANCE), 0, ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(TOOLINSPECTION_FINISHED),ToolInspectionFinished.class);
            aas = (AgentActionSchema) getSchema(TOOLINSPECTION_FINISHED);
            aas.add(TOOLINSPECTION_FINISHED_TOOLS, (ConceptSchema) getSchema(TOOLINSTANCE), 0, ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(DISPLAY_COMMISSION),DisplayCommissionList.class);
            aas = (AgentActionSchema) getSchema(DISPLAY_COMMISSION);
            aas.add(DISPLAY_COMMISSION_TOOLS, (ConceptSchema) getSchema(TOOLINSTANCE), 0, ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(TAKEOUT_TOOLS),TakeOutTools.class);
            aas = (AgentActionSchema) getSchema(TAKEOUT_TOOLS);
            aas.add(TAKEOUT_TOOLS_TOOLS, (ConceptSchema) getSchema(TOOLINSTANCE), 0, ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(MAKE_TOOLSUPPLY_ORDER),MakeToolSupplyOrder.class);
            aas = (AgentActionSchema) getSchema(MAKE_TOOLSUPPLY_ORDER);
            aas.add(MAKE_TOOLSUPPLY_ORDER_TOOLREQUIREMENTS, (ConceptSchema) getSchema(TOOLREQUIREMENTSLIST));
            
            add(new AgentActionSchema(DELETETOOLS),DeleteTools.class);
            aas = (AgentActionSchema) getSchema(DELETETOOLS);
            aas.add(DELETETOOLS_TOOLINSTANCES, (ConceptSchema) getSchema(TOOLINSTANCE),0,ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(SETUP_COMPLETE),SetupComplete.class);
            
            
            //~~~~~~~~~~~~~~~~~~~~~~~PREDICATES~~~~~~~~~~~~~~~~~~~~~~~~~~
            add(new PredicateSchema(NCPROGRAM_REFERENCES_SHEET),NcProgramReferencesSheet.class);
            PredicateSchema ps = (PredicateSchema) getSchema(NCPROGRAM_REFERENCES_SHEET);
            ps.add(NCPROGRAM_REFERENCES_SHEET_NCPROGRAMNR, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            ps.add(NCPROGRAM_REFERENCES_SHEET_SETTINGSHEET, (ConceptSchema) getSchema(SETTINGSHEET));
            
            add(new PredicateSchema(TOOL_SUPPLY_TIMES),ToolSupplyTimes.class);
            ps = (PredicateSchema) getSchema(TOOL_SUPPLY_TIMES);
            ps.add(TOOL_SUPPLY_TIMES_SETUPTIMELISTS, (ConceptSchema) getSchema(SETUPTIMEENTRY), 0, ObjectSchema.UNLIMITED);
            
            add(new PredicateSchema(GENERATED_PICKINGLIST),GeneratedPickingList.class);
            ps = (PredicateSchema) getSchema(GENERATED_PICKINGLIST);
            ps.add(GENERATED_PICKINGLIST_PICKINGLIST, (ConceptSchema) getSchema(PICKINGLIST));
            ps.add(GENERATED_PICKINGLIST_NEXTVIABLEINVID, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            
            add(new PredicateSchema(STOREDTOOLS),StoredTools.class);
            ps = (PredicateSchema) getSchema(STOREDTOOLS);
            ps.add(STOREDTOOLS_TOOLINSTANCES, (ConceptSchema) getSchema(TOOLINSTANCE), 0, ObjectSchema.UNLIMITED);
            
            add(new PredicateSchema(FOUNDTOOLS),FoundTools.class);
            ps = (PredicateSchema) getSchema(FOUNDTOOLS);
            ps.add(FOUNDTOOLS_TOOLINSTANCES, (ConceptSchema) getSchema(TOOLINSTANCE), 0, ObjectSchema.UNLIMITED);
            
            //!!!!!!!!!!!!!!!!!!!!!!!TESTING!!!!!!!!!!!!!!!!!!!!!
            add(new ConceptSchema(MACHINEORDER_ENTRY), MachineOrderEntry.class);
            cs = (ConceptSchema) getSchema(MACHINEORDER_ENTRY);
            cs.add(MACHINEORDER_ENTRY_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(MACHINEORDER_ENTRY_SETTINGSHEET, (ConceptSchema) getSchema(SETTINGSHEET));
            cs.add(MACHINEORDER_ENTRY_PROJECTEDCOST, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(MACHINEORDER_ENTRY_PROJECTEDTIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(MACHINEORDER_ENTRY_PROJECTEDSAVINGS, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(MACHINEORDER_ENTRY_TOOLED, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
            cs.add(MACHINEORDER_ENTRY_PROJECTEDUSEDTOOLS
                    , (ConceptSchema) getSchema(TOOLREQUIREMENTSLIST));
            cs.add(MACHINEORDER_ENTRY_NCPROGRAMR, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(MACHINEORDER_ENTRY_MAXCOST, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(MACHINEORDER_ENTRY_MAXTIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(MACHINEORDER_ENTRY_QUANTITY, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            cs.add(MACHINEORDER_ENTRY_LATENESSALLOWANCEFACTOR, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            
            add(new PredicateSchema(MAGAZINE_SETUP_TEST_RESPONSE), MagazineSetupTestResponse.class);
            ps = (PredicateSchema) getSchema(MAGAZINE_SETUP_TEST_RESPONSE);
            ps.add(MAGAZINE_SETUP_TEST_RESPONSE_TOOMANYTOOLS, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
            
            ps.add(MAGAZINE_SETUP_TEST_RESPONSE_TOOLITTLELIFETIME, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
            ps.add(MAGAZINE_SETUP_TEST_RESPONSE_MAGAZINE, (ConceptSchema) getSchema(TOOLINSTANCE), 0, ObjectSchema.UNLIMITED);            
            
            add(new AgentActionSchema(SET_TOOLS), SetTools.class);
            aas = (AgentActionSchema) getSchema(SET_TOOLS);
            aas.add(SET_TOOLS_TOOLINSTANCES, (ConceptSchema) getSchema(TOOLINSTANCE),0,ObjectSchema.UNLIMITED);
            aas.add(SET_TOOLS_STORED, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
            
            add(new AgentActionSchema(SET_MACHINEQUEUE_ENTRIES), SetMachineQueueEntries.class);
            aas = (AgentActionSchema) getSchema(SET_MACHINEQUEUE_ENTRIES);
            aas.add(SET_MACHINEQUEUE_ENTRIES_MACHINEORDERENTRIES, (ConceptSchema) getSchema(MACHINEORDER_ENTRY),0,ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(START_MACHINESETUP_INFO_REQUEST_TEST), StartMachineSetupInfoRequestTest.class);
            aas = (AgentActionSchema) getSchema(START_MACHINESETUP_INFO_REQUEST_TEST);
            aas.add(START_MACHINESETUP_INFO_REQUEST_TEST_NCPROGRAMNR, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            aas.add(START_MACHINESETUP_INFO_REQUEST_TEST_MAXCOST, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            aas.add(START_MACHINESETUP_INFO_REQUEST_TEST_MAXTIME, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            aas.add(START_MACHINESETUP_INFO_REQUEST_TEST_QUANTITY, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            aas.add(START_MACHINESETUP_INFO_REQUEST_TEST_LATENESSALLOWANCEFACTOR, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            
            add(new AgentActionSchema(START_MAGAZINE_SETUP_TEST), StartMagazineSetupTest.class);
            aas = (AgentActionSchema) getSchema(START_MAGAZINE_SETUP_TEST);
            aas.add(START_MAGAZINE_SETUP_TEST_TOOLS, (ConceptSchema) getSchema(TOOLINSTANCE),0,ObjectSchema.UNLIMITED);
            
            add(new AgentActionSchema(START_TOOL_REQUISITION_TEST), StartToolRequisitionTest.class);
            aas = (AgentActionSchema) getSchema(START_TOOL_REQUISITION_TEST);
            aas.add(START_TOOL_REQUISITION_TEST_POSITION, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
        
        }catch(OntologyException e){
            logger.severe("Tool Ontology could not be built");
        }
        
    }
    
    public static Ontology getInstance(){
        return instance;
    }
}
