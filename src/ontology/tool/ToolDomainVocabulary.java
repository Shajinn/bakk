
package ontology.tool;

/**
 *
 * @author Shahin Mahmody
 */
public interface ToolDomainVocabulary {
    //~~~~~~~~~~~~~~~~~~~~~~~CONCEPTS~~~~~~~~~~~~~~~~~~~~~~~~~~
    // a type of tool with its core data
    public static final String TOOL = "Tool";
    public static final String TOOL_ID = "toolId";
    public static final String TOOL_LIFETIME = "lifetime";
    public static final String TOOL_CRITICALTIME = "criticalTime";
    public static final String TOOL_NAME = "name";
    
    // components required for a tool's assembly
    public static final String COMPONENT = "Component";
    public static final String COMPONENT_ID = "componentId";
    public static final String COMPONENT_TOOL = "toolId";
    public static final String COMPONENT_NAME = "name";
    public static final String COMPONENT_DESCRIPTION = "description";
    
    // entry of components and their quantity
    public static final String COMPONENTENTRY = "ComponentEntry";
    public static final String COMPONENTENTRY_COMPONENT = "component";
    public static final String COMPONENTENTRY_QUANTITY = "quantity";
    
    // a concrete real life finished tool
    public static final String TOOLINSTANCE = "ToolInstance";
    public static final String TOOLINSTANCE_ID = "instanceId";
    public static final String TOOLINSTANCE_TOOL = "tool";
    public static final String TOOLINSTANCE_USEDTIME = "usedTime";
    public static final String TOOLINSTANCE_LENGTHDEVIATION = "lengthDeviation";
    public static final String TOOLINSTANCE_RADIUSDEVIATION = "radiusDeviation";
    
    //Tool Entry
    public static final String TOOLREQUIREMENTENTRY = "ToolRequirementEntry";
    public static final String TOOLREQUIREMENTENTRY_TOOL = "tool";
    public static final String TOOLREQUIREMENTENTRY_USETIME = "usetime";
    public static final String TOOLREQUIREMENTENTRY_QUANTITY = "quantity";
    public static final String TOOLREQUIREMENTENTRY_TEMPLATETOOLLIFE = "templateToolLife";
    
    //Tool requirements
    public static final String TOOLREQUIREMENTSLIST = "ToolRequirementList";
    public static final String TOOLREQUIREMENTSLIST_TOOLREQUIREMENTS = "toolRequirements";
    public static final String TOOLREQUIREMENTSLIST_REQUIREMENTTYPE = "requirementType";
    
    //Setting Sheet
    public static final String SETTINGSHEET = "SettingSheet";
    public static final String SETTINGSHEET_NAME = "name";
    public static final String SETTINGSHEET_NCPROGRAMNR = "ncProgramNr";
    public static final String SETTINGSHEET_THROUGHPUTTIME = "throughputTime";
    public static final String SETTINGSHEET_SETUPTIME = "setupTime";
    public static final String SETTINGSHEET_TOOLREQUIREMENTSLIST = "toolRequirementsList";

    //Picking List
    public static final String PICKINGLIST = "PickingList";
    public static final String PICKINGLIST_COMPONENTS = "components";
    
    //Picking List Entry
    public static final String PICKINGLISTENTRY = "PickingListEntry";
    public static final String PICKINGLISTENTRY_COMPONENT = "component";
    public static final String PICKINGLISTENTRY_QUANTITY = "quantity";
    public static final String PICKINGLISTENTRY_TOOL ="tool";
    
    //Usetime List
    public static final String USETIMELIST = "UsetimeList";
    public static final String USETIMELIST_TOOLUSAGES = "toolUsages";
    public static final String USETIMELIST_POSITION = "position";
    
    //Usetime List Entry
    public static final String USETIMELISTENTRY = "UsetimeListEntry";
    public static final String USETIMELISTENTRY_ID = "id";
    public static final String USETIMELISTENTRY_TOOLREQUIREMENTLIST = "toolRequirementList";
    public static final String USETIMELISTENTRY_MACHININGTIME = "machiningTime";
    public static final String USETIMELISTENTRY_PREVIOUSORDERMACHININGTIME = "previousOrderMachiningTime";
    
    //Usetime List list
    public static final String USETIMELISTLIST = "UsetimeListList";
    public static final String USETIMELISTLIST_USETIMELISTS = "usetimeLists";
    
    //Setup time for an order
    public static final String ORDERSETUPTIME = "OrderSetupTime";
    public static final String ORDERSETUPTIME_ORDERID = "orderId";
    public static final String ORDERSETUPTIME_TIMEREQUIRED = "timeRequired";
    public static final String ORDERSETUPTIME_STOREDTOOLSAMOUNT = "storedToolsAmount";
    public static final String ORDERSETUPTIME_NEWTOOLSAMOUNT = "newToolsAmount";
    public static final String ORDERSETUPTIME_TOOLREQUIREMENTDETAILS = "toolRequirementDetails";
    
    
    public static final String SETUPTIMEENTRY = "SetupTimeEntry";
    public static final String SETUPTIMEENTRY_TIMESLIST = "timesList";
    public static final String SETUPTIMEENTRY_POSITION = "position";
    
    //~~~~~~~~~~~~~~~~~~~~~~~ACTIONS~~~~~~~~~~~~~~~~~~~~~~~~~~
    // DB Setting Sheet Query
    public static final String QUERY_SETTINGSHEET = "QuerySettingSheet";
    public static final String QUERY_SETTINGSHEET_NCPROGRAMNR = "ncProgramNr";
    
    public static final String CALCULATE_TOOLSTOCKFORPROJECTEDORDERS = "CalculateToolStockForProjectedOrders";
    public static final String CALCULATE_TOOLSTOCKFORPROJECTEDORDERS_USETIMELISTS = "usetimeLists";
    
    // Setup time of all required tools Query
    public static final String REQUEST_TOOLSUPPLY_TIME = "RequestToolSupplyTime";
    public static final String REQUEST_TOOLSUPPLY_TIME_USETIMELISTS = "usetimeLists";
    
    // Requisition Tools
    public static final String REQUISITION_TOOLS = "RequisitionTools";
    public static final String REQUISITION_TOOLS_TOOLINSTANCES = "toolInstances";
    
    // get generated Picking List from DB
    public static final String QUERY_COMPONENTS_FOR_DYNAMIC_SETTINGSHEET = "QueryComponentsForDynamicSettingSheet";
    public static final String QUERY_COMPONENTS_FOR_DYNAMIC_SETTINGSHEET_TOOLREQUIREMENTS = "toolRequirementsList";
    
    // get existing ToolInstances for the setting sheet
    public static final String QUERY_STOREDTOOLS = "QueryStoredTools";
    public static final String QUERY_STOREDTOOLS_TOOLS = "tools";
    
    //display picking list
    public static final String DISPLAY_PICKINGLIST = "DisplayPickingList";
    public static final String DISPLAY_PICKINGLIST_PICKINGLIST = "pickingList";
    public static final String DISPLAY_PICKINGLIST_NEXTVIABLEINVID = "nextViableInvId";
    
    //store tools in db
    public static final String STORE_TOOLS = "StoreTools";
    public static final String STORE_TOOLS_INSTANCES = "toolInstances";
    public static final String STORE_TOOLS_JUSTASSEMBLED = "justAssembled";
    
    //Tools have been assembled
    public static final String TOOLASSEMBLY_FINISHED = "ToolAssemblyFinished";
    public static final String TOOLASSEMBLY_FINISHED_TOOLS = "toolInstances";
    
    //Tools have been inspected
    public static final String TOOLINSPECTION_FINISHED = "ToolInspectionFinished";
    public static final String TOOLINSPECTION_FINISHED_TOOLS = "toolInstances";
    
    //measure tools
    public static final String DISPLAY_COMMISSION = "DisplayCommission";
    public static final String DISPLAY_COMMISSION_TOOLS = "toolInstances";
    
    //measure tools
    public static final String TAKEOUT_TOOLS = "TakeOutTools";
    public static final String TAKEOUT_TOOLS_TOOLS = "toolInstances";
    
    //Setup complete
    public static final String SETUP_COMPLETE = "SetupComplete";
    
    public static final String MAKE_TOOLSUPPLY_ORDER = "MakeToolSupplyOrder";
    public static final String MAKE_TOOLSUPPLY_ORDER_TOOLREQUIREMENTS = "toolRequirements";
    
    public static final String DELETETOOLS = "DeleteTools";
    public static final String DELETETOOLS_TOOLINSTANCES = "toolInstances";
    
    //~~~~~~~~~~~~~~~~~~~~~~~PREDICATES~~~~~~~~~~~~~~~~~~~~~~~~~~
    //Nc Program references Setting Sheet
    public static final String NCPROGRAM_REFERENCES_SHEET = "NcProgramReferencesSheet";
    public static final String NCPROGRAM_REFERENCES_SHEET_NCPROGRAMNR = "ncProgramNr";
    public static final String NCPROGRAM_REFERENCES_SHEET_SETTINGSHEET = "settingSheet";
    
    //These tools would take this long to set up
    public static final String TOOL_SUPPLY_TIMES = "ToolSupplyTimes";
    public static final String TOOL_SUPPLY_TIMES_SETUPTIMELISTS = "setupTimeLists";
    
    //The picking list for a given setting sheet
    public static final String GENERATED_PICKINGLIST = "GeneratedPickingList";
    public static final String GENERATED_PICKINGLIST_PICKINGLIST = "pickingList";
    public static final String GENERATED_PICKINGLIST_NEXTVIABLEINVID = "nextViableInvId";
    
    //existing tools to a given setting sheets
    public static final String STOREDTOOLS_FOR_SETTINGSHEET = "StoredToolsForSettingSheet";
    public static final String STOREDTOOLS_FOR_SETTINGSHEET_SETTINGSHEET = "settingSheet";
    public static final String STOREDTOOLS_FOR_SETTINGSHEET_TOOLS = "tools";
    
    //existing tools
    public static final String STOREDTOOLS = "StoredTools";
    public static final String STOREDTOOLS_TOOLINSTANCES = "toolInstances";
    
    //adjusted usetime lists
    public static final String ADJUSTEDTOOLREQUIREMENTS = "AdjustedToolRequirements";
    public static final String ADJUSTEDTOOLREQUIREMENTS_USETIMELISTS = "usetimeLists";
    
    public static final String FOUNDTOOLS = "FoundTools";
    public static final String FOUNDTOOLS_TOOLINSTANCES = "toolInstances";
    //~~~~~~~~~~~~~~~~~~~~~~~SERVICES & PROTOCOLS~~~~~~~~~~~~~~~~~~~~~~~~~~
    public static final String DATABASE_SERVICE = "database_service";
    public static final String SUPPLY_SERVICE = "supply_service";
    public static final String VISUAL_SERVICE = "visual_service";
    
    public static final String MACHINE_SETUP_INFO_REQUEST_PROTOCOL = "MachineSetupInfoRequest";
    public static final String SETUP_INFORMATION_PROTOCOL = "SetupInformationProtocol";
    public static final String TOOL_REQUISITION_PROTOCOL = "ToolRequisition";
    public static final String MAGAZINE_SETUP_PROTOCOL = "MagazineSetupProtocol";
    
    
    
    //!!!!!!!!!!!!!!!!!!!!!!!TESTING!!!!!!!!!!!!!!!!!!!!!
    public static final String TEST_PROTOCOL = "TestProtocol";
    
    public static final String MACHINEORDER_ENTRY = "MachineOrderEntry";
    public static final String MACHINEORDER_ENTRY_ID = "id";
    public static final String MACHINEORDER_ENTRY_SETTINGSHEET = "settingSheet";
    public static final String MACHINEORDER_ENTRY_PROJECTEDCOST = "projectedCost";
    public static final String MACHINEORDER_ENTRY_PROJECTEDTIME = "projectedTime";
    public static final String MACHINEORDER_ENTRY_PROJECTEDSAVINGS = "projectedSavings";
    public static final String MACHINEORDER_ENTRY_TOOLED = "tooled";
    public static final String MACHINEORDER_ENTRY_PROJECTEDUSEDTOOLS = "projectedUsedTools";
    public static final String MACHINEORDER_ENTRY_NCPROGRAMR = "ncProgramNr";
    public static final String MACHINEORDER_ENTRY_MAXCOST = "maxCost";
    public static final String MACHINEORDER_ENTRY_MAXTIME = "maxTime";
    public static final String MACHINEORDER_ENTRY_QUANTITY = "quantity";
    public static final String MACHINEORDER_ENTRY_LATENESSALLOWANCEFACTOR = "latenessAllowanceFactor";
    
    public static final String SET_TOOLS = "SetTools";
    public static final String SET_TOOLS_TOOLINSTANCES = "toolInstances";
    public static final String SET_TOOLS_STORED = "stored";
    
    public static final String SET_MACHINEQUEUE_ENTRIES = "SetMachineQueueEntries";
    public static final String SET_MACHINEQUEUE_ENTRIES_MACHINEORDERENTRIES = "machineOrderEntries";
    
    public static final String START_MACHINESETUP_INFO_REQUEST_TEST = "StartMachineSetupInfoRequestTest";
    public static final String START_MACHINESETUP_INFO_REQUEST_TEST_NCPROGRAMNR = "ncProgramNr";
    public static final String START_MACHINESETUP_INFO_REQUEST_TEST_MAXCOST = "maxCost";
    public static final String START_MACHINESETUP_INFO_REQUEST_TEST_MAXTIME = "maxTime";
    public static final String START_MACHINESETUP_INFO_REQUEST_TEST_QUANTITY = "quantity";
    public static final String START_MACHINESETUP_INFO_REQUEST_TEST_LATENESSALLOWANCEFACTOR = "latenessAllowanceFactor";
    
    public static final String START_MAGAZINE_SETUP_TEST = "StartMagazineSetupTest";
    public static final String START_MAGAZINE_SETUP_TEST_TOOLS = "tools";
    
    public static final String START_TOOL_REQUISITION_TEST = "StartToolRequisitionTest";
    public static final String START_TOOL_REQUISITION_TEST_POSITION = "position";
    
    public static final String MAGAZINE_SETUP_TEST_RESPONSE = "MagazineSetupTestResponse";
    public static final String MAGAZINE_SETUP_TEST_RESPONSE_TOOMANYTOOLS = "tooManyTools";
    public static final String MAGAZINE_SETUP_TEST_RESPONSE_TOOLITTLELIFETIME = "tooLittleLifetime";
    public static final String MAGAZINE_SETUP_TEST_RESPONSE_MAGAZINE = "magazine";
    
}
