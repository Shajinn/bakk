
package testing;

import jade.gui.GuiEvent;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import ontology.tool.terms.SettingSheet;
import ontology.tool.terms.Tool;
import ontology.tool.terms.ToolInstance;
import ontology.tool.terms.ToolRequirementEntry;
import ontology.tool.terms.ToolRequirementList;

/**
 *
 * @author Shahin Mahmody
 */
public class TestInterface extends JFrame implements ActionListener{
    private final TestingAgent myAgent;
    
    private JButton setEmptyScenarioBtn;
    private JButton setSystemTestScenarioBtn;
    private JButton setSystemTestScenario3Btn;
    private JButton setSystemTestScenario4Btn;
    private JButton emptyMagToolsBtn;
    private JButton setMagToolsBtn;
    private JButton testMachineSetupInfoProtocolBtn;
    private JButton scenario1_2SetupInfoTestBtn;
    
    
    private final TestScenario emptyScenario;
    private final TestScenario basicNC12072018Scenario;
    private final TestScenario scenario1_2NoAction; 
    private final TestScenario scenario3NoAction;
    private final TestScenario scenario4NoAction;
    private final TestScenario scenario1_2SetupInfoTest;
    
    //TOOLS
    private final Tool drillD13 = new Tool("8750", 100, 2, "DrillD13");
    private final Tool shellMillD66 = new Tool("8777", 93, 2.75f, "ShellMillD66");
    private final Tool cylindricShaftMillD10 = new Tool("8768", 79.5f, 2.95f, "CylindricShaftMillD10");
    private final Tool millingCutterD925 = new Tool("8789", 70, 2.5f, "MillingCutterD9,25");
    private final Tool millingCutterD10 = new Tool("8780", 98.5f, 3, "MillingCutterD10");
    private final Tool millingCutterD16 = new Tool("8783", 96, 2.5f, "MillingCutterD16");
    private final Tool millingCutterD3171 = new Tool("8795", 59, 2.9f, "MillingCutterD31,71");
    private final Tool faceMillD60 = new Tool("8759", 85, 3.5f, "FaceMillD60");
    private final Tool threadMillM5X8 = new Tool("8741", 70, 4, "ThreadMillM5X8"); 
    private final Tool countersinkD9 =  new Tool("8786", 98, 2.8f, "CountersinkD9"); 
    private final Tool stirnfräserD60 =  new Tool("8350", 80, 4, "StirnfräserD60");
    private final Tool faceMillD160 =  new Tool("8762", 83, 3, "FaceMillD160"); 
    private final Tool roundShankDrillD85 =  new Tool("8805", 58.5f, 1.85f, "RoundShankDrillD8,5"); 
    private final Tool drillD127 = new Tool("8814",57.5f,1.8f,"DrillD12,7");
    
    //Setup Info
    private JButton setupInfoScenario1Btn;// no queue
    private JButton setupInfoScenario2Btn;//1 in queue fix
    private JButton setupInfoScenario3Btn;//2 in queue fix
    private JButton setupInfoScenario4Btn;//multiple choice
    private JButton setupInfoScenario5Btn;//setup too long 
    
    private final TestScenario setupInfoScenario1;
    private final TestScenario setupInfoScenario2;
    private final TestScenario setupInfoScenario3;
    private final TestScenario setupInfoScenario4;
    private final TestScenario setupInfoScenario5;
    
    //Tool Requisition
    private JButton toolRequisitionScenario1Btn;//calculation
    private JButton toolRequisitionScenario2Btn;//too few tools in storage
    
    private final TestScenario toolRequisitionScenario1;
    private final TestScenario toolRequisitionScenario2;
    
    
    //Magazine Setup
    private JButton magazineSetupScenario1Btn;//calculation
    private JButton magazineSetupScenario2Btn;//too little lifetime
    private JButton magazineSetupScenario3Btn;//too many tools
    
    private final TestScenario magazineSetupScenario1;
    private final TestScenario magazineSetupScenario2;
    private final TestScenario magazineSetupScenario3;
    
    
    
    public TestInterface(TestingAgent agent){
        this.myAgent = agent;
        
        
        emptyScenario = new TestScenario();
        
        basicNC12072018Scenario = new TestScenario();
        MachineOrderEntry entry = new MachineOrderEntry();
        entry.setId("test");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");      
        basicNC12072018Scenario.machineQueue.add(entry);
        basicNC12072018Scenario.filledTestAction = new StartToolRequisitionTest(0);
        
        scenario1_2NoAction = new TestScenario();
        scenario1_2NoAction.dbTools.add(new ToolInstance(new Tool("8783", 96, 2.5f, "MillingCutterD16"), "353", 6, 0, 0));
        scenario1_2NoAction.dbTools.add(new ToolInstance(new Tool("8757", 85, 5, "ThreadMillD17"), "354", 12, 0, 0));  
        scenario1_2NoAction.magazineTools.add(new ToolInstance(new Tool("8731", 90, 3, "ShankMillD20"), "349", 3, 0, 0));
        scenario1_2NoAction.magazineTools.add(new ToolInstance(new Tool("8757", 85, 5, "ThreadMillD17"), "350", 20, 0, 0));
        scenario1_2NoAction.magazineTools.add(new ToolInstance(new Tool("8774", 100, 2, "SlottingCutterD124"), "351", 11, 0, 0));
        scenario1_2NoAction.magazineTools.add(new ToolInstance(new Tool("8792", 60, 1.5f, "MillingCutterD15,5"), "352", 3, 0, 0));
        scenario1_2NoAction.filledTestAction = null;
        
        scenario1_2SetupInfoTest = new TestScenario();
        scenario1_2SetupInfoTest.magazineTools = scenario1_2NoAction.magazineTools;
        scenario1_2SetupInfoTest.dbTools = scenario1_2NoAction.dbTools;
        scenario1_2SetupInfoTest.machineQueue = scenario1_2NoAction.machineQueue;
        scenario1_2SetupInfoTest.filledTestAction = new StartMachineSetupInfoRequestTest("NC12072018",2000,200,10,1.45f);
       
        scenario4NoAction = new TestScenario();
        scenario4NoAction.magazineTools.add(new ToolInstance(new Tool("8750", 100, 2, "DrillD13"), "357", 8, 0, 0));
        scenario4NoAction.magazineTools.add(new ToolInstance(new Tool("8777", 93, 2.95f, "ShellMillD66"), "358", 4, 0, 0));
        scenario4NoAction.magazineTools.add(new ToolInstance(new Tool("8774", 100, 2, "SlottingCutterD124"), "351", 11, 0, 0));
        scenario4NoAction.magazineTools.add(new ToolInstance(new Tool("8786", 98, 2.8f, "CountersinkD9"), "359", 10, 0, 0));
        scenario4NoAction.magazineTools.add(new ToolInstance(new Tool("8795", 59, 2.9f, "MillingCutterD31,71"), "299", 0, 0, 0));
        scenario4NoAction.magazineTools.add(new ToolInstance(new Tool("8759", 85, 3.5f, "FaceMillD60"), "300", 5, 0, 0));
        scenario4NoAction.magazineTools.add(new ToolInstance(new Tool("8741", 70, 4, "ThreadMillM5X8"), "301", 7, 0, 0));
        scenario4NoAction.magazineTools.add(new ToolInstance(new Tool("8783", 96, 2.5f, "MillingCutterD16"), "302", 7, 0, 0));
        scenario4NoAction.magazineTools.add(new ToolInstance(new Tool("8780", 98.5f, 3, "MillingCutterD10"), "303", 7.5f, 0, 0));
        scenario4NoAction.magazineTools.add(new ToolInstance(new Tool("8768", 79.5f, 2.95f, "CylindricShaftMillD10"), "360", 4, 0, 0));
        scenario4NoAction.filledTestAction = null;
        
        scenario3NoAction = new TestScenario(); 
        scenario3NoAction.magazineTools.add(new ToolInstance(new Tool("8750", 100, 2, "DrillD13"), "355", 90, 0, 0));
        scenario3NoAction.magazineTools.add(new ToolInstance(new Tool("8777", 93, 2.95f, "ShellMillD66"), "356", 63, 0, 0));
        scenario3NoAction.magazineTools.add(new ToolInstance(new Tool("8789", 70, 2.5f, "MillingCutterD9,25"), "306", 2, 0, 0));
        scenario3NoAction.magazineTools.add(new ToolInstance(new Tool("8768", 79.5f, 2.95f, "CylindricalShaftMillD10"), "307", 0, 0, 0));
        scenario3NoAction.magazineTools.add(new ToolInstance(new Tool("8795", 59, 2.9f, "MillingCutterD31,71"), "299", 0, 0, 0));
        scenario3NoAction.magazineTools.add(new ToolInstance(new Tool("8759", 85, 3.5f, "FaceMillD60"), "300", 5, 0, 0));
        scenario3NoAction.magazineTools.add(new ToolInstance(new Tool("8741", 70, 4, "ThreadMillM5X8"), "301", 7, 0, 0));
        scenario3NoAction.magazineTools.add(new ToolInstance(new Tool("8783", 96, 2.5f, "MillingCutterD16"), "302", 7, 0, 0));
        scenario3NoAction.magazineTools.add(new ToolInstance(new Tool("8780", 98.5f, 3, "MillingCutterD10"), "303", 7.5f, 0, 0));
        scenario3NoAction.filledTestAction = null;
        
        
        setupInfoScenario1 = new TestScenario();
        setupInfoScenario1.filledTestAction = new StartMachineSetupInfoRequestTest("NC12072018", 200, 2000, 10, 1.45f);
        
        setupInfoScenario2 = new TestScenario();
        entry = new MachineOrderEntry();
        entry.setId("test0");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(7.74f);
        entry.setQuantity(10);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        setupInfoScenario2.machineQueue.add(entry);
        setupInfoScenario2.filledTestAction = new StartMachineSetupInfoRequestTest("NC12072018", 200, 2000, 10, 1.45f);
        
        setupInfoScenario3 = new TestScenario();
        //QUEUE 1
        entry = new MachineOrderEntry();
        entry.setId("test0");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(7.74f);
        entry.setQuantity(10);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        setupInfoScenario3.machineQueue.add(entry);
        //QUEUE 2
        entry = new MachineOrderEntry();
        entry.setId("test1");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(7.74f);
        entry.setQuantity(10);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        setupInfoScenario3.machineQueue.add(entry);
        setupInfoScenario3.filledTestAction = new StartMachineSetupInfoRequestTest("NC12072018", 200, 2000, 10, 1.45f);
        
        setupInfoScenario4 = new TestScenario();
        setupInfoScenario4.dbTools.add(new ToolInstance(faceMillD60, "1000", 0, 0, 0));
        //QUEUE 1
        entry = new MachineOrderEntry();
        entry.setId("test0");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(7.74f);
        entry.setQuantity(10);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        setupInfoScenario4.machineQueue.add(entry);
        //QUEUE 2
        entry = new MachineOrderEntry();
        entry.setId("test1");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(7.74f);
        entry.setQuantity(10);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        setupInfoScenario4.machineQueue.add(entry);
        //QUEUE 3
        entry = new MachineOrderEntry();
        entry.setId("test2");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(7.74f);
        entry.setQuantity(10);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        setupInfoScenario4.machineQueue.add(entry);
        //QUEUE 4
        entry = new MachineOrderEntry();
        entry.setId("test3");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072019");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(7.74f);
        entry.setQuantity(10);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("ob"));
        entry.setTooled(false);
        setupInfoScenario4.machineQueue.add(entry);
        setupInfoScenario4.filledTestAction = new StartMachineSetupInfoRequestTest("NC12072018", 200, 2000, 10, 1.45f);
        
        
        setupInfoScenario5 = new TestScenario();
        entry = new MachineOrderEntry();
        entry.setId("test0");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(1);
        entry.setQuantity(1);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(true);
        setupInfoScenario5.machineQueue.add(entry);
        setupInfoScenario5.filledTestAction = new StartMachineSetupInfoRequestTest("NC12072018", 200, 2000, 10, 1.45f);
        
        
        toolRequisitionScenario1 = new TestScenario();
        toolRequisitionScenario1.dbTools.add(new ToolInstance(drillD13, "353", 6, 0, 0));
        toolRequisitionScenario1.dbTools.add(new ToolInstance(shellMillD66  , "354", 12, 0, 0));  
        entry = new MachineOrderEntry();
        entry.setId("test0");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(1);
        entry.setQuantity(1);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.getProjectedUsedTools().getToolRequirements().add(new ToolRequirementEntry(drillD13, 0, 1, 0));
        entry.getProjectedUsedTools().getToolRequirements().add(new ToolRequirementEntry(shellMillD66, 0, 1, 0));
        entry.getProjectedUsedTools().getToolRequirements().add(new ToolRequirementEntry(cylindricShaftMillD10,7.4f ,0 , 0));
        entry.getProjectedUsedTools().getToolRequirements().add(new ToolRequirementEntry(millingCutterD925, 8.755f, 0, 0));
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        toolRequisitionScenario1.machineQueue.add(entry);
        toolRequisitionScenario1.filledTestAction = new StartToolRequisitionTest(0);
        
        
        toolRequisitionScenario2 = new TestScenario();
        toolRequisitionScenario1.dbTools.add(new ToolInstance(shellMillD66  , "354", 12, 0, 0));  
        entry = new MachineOrderEntry();
        entry.setId("test0");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(1);
        entry.setQuantity(1);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.getProjectedUsedTools().getToolRequirements().add(new ToolRequirementEntry(drillD13, 0, 1, 0));
        entry.getProjectedUsedTools().getToolRequirements().add(new ToolRequirementEntry(shellMillD66, 0, 1, 0));
        entry.getProjectedUsedTools().getToolRequirements().add(new ToolRequirementEntry(cylindricShaftMillD10,7.4f ,0 , 0));
        entry.getProjectedUsedTools().getToolRequirements().add(new ToolRequirementEntry(millingCutterD925, 8.755f, 0, 0));
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        toolRequisitionScenario2.machineQueue.add(entry);
        toolRequisitionScenario2.filledTestAction = new StartToolRequisitionTest(0);
        
        
        magazineSetupScenario1 = new TestScenario();
        entry = new MachineOrderEntry();
        entry.setId("test0");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(1);
        entry.setQuantity(10);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        magazineSetupScenario1.magazineTools.add(new ToolInstance(drillD127, "1000", 0, 0, 0));
        magazineSetupScenario1.magazineTools.add(new ToolInstance(drillD13, "1001", 95, 0, 0));
        magazineSetupScenario1.magazineTools.add(new ToolInstance(shellMillD66, "1002", 33, 0, 0));
        magazineSetupScenario1.magazineTools.add(new ToolInstance(cylindricShaftMillD10, "1003", 0, 0, 0));
        magazineSetupScenario1.magazineTools.add(new ToolInstance(drillD127, "1004", 0, 0, 0));
        magazineSetupScenario1.magazineTools.add(new ToolInstance(drillD127, "1005", 0, 0, 0));
        magazineSetupScenario1.magazineTools.add(new ToolInstance(drillD127, "1006", 0, 0, 0));
        magazineSetupScenario1.magazineTools.add(new ToolInstance(drillD127, "1007", 0, 0, 0));
        magazineSetupScenario1.magazineTools.add(new ToolInstance(drillD127, "1008", 0, 0, 0));
        magazineSetupScenario1.magazineTools.add(new ToolInstance(drillD127, "1009", 0, 0, 0));
        magazineSetupScenario1.machineQueue.add(entry);
        ArrayList broughtTools = new ArrayList();
        broughtTools.add(new ToolInstance(drillD13,"1011",0,0,0));
        broughtTools.add(new ToolInstance(millingCutterD925,"1012",63,0,0));
        broughtTools.add(new ToolInstance(millingCutterD925,"1013",63,0,0));
        magazineSetupScenario1.filledTestAction = new StartMagazineSetupTest(broughtTools);
        
        
        magazineSetupScenario2 = new TestScenario();
        entry = new MachineOrderEntry();
        entry.setId("test0");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(1);
        entry.setQuantity(10);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        magazineSetupScenario2.magazineTools.add(new ToolInstance(drillD127, "1000", 0, 0, 0));
        magazineSetupScenario2.magazineTools.add(new ToolInstance(drillD13, "1001", 95, 0, 0));
        magazineSetupScenario2.magazineTools.add(new ToolInstance(shellMillD66, "1002", 33, 0, 0));
        magazineSetupScenario2.magazineTools.add(new ToolInstance(cylindricShaftMillD10, "1003", 0, 0, 0));
        magazineSetupScenario2.magazineTools.add(new ToolInstance(drillD127, "1004", 0, 0, 0));
        magazineSetupScenario2.magazineTools.add(new ToolInstance(drillD127, "1005", 0, 0, 0));
        magazineSetupScenario2.magazineTools.add(new ToolInstance(drillD127, "1006", 0, 0, 0));
        magazineSetupScenario2.magazineTools.add(new ToolInstance(drillD127, "1007", 0, 0, 0));
        magazineSetupScenario2.magazineTools.add(new ToolInstance(drillD127, "1008", 0, 0, 0));
        magazineSetupScenario2.magazineTools.add(new ToolInstance(drillD127, "1009", 0, 0, 0));
        magazineSetupScenario2.machineQueue.add(entry);
        ArrayList broughtTools2 = new ArrayList();
        broughtTools2.add(new ToolInstance(drillD13,"1011",0,0,0));
        broughtTools2.add(new ToolInstance(millingCutterD925,"1012",63,0,0));
       // broughtTools.add(new ToolInstance(millingCutterD925,"1013",63,0,0));
        magazineSetupScenario2.filledTestAction = new StartMagazineSetupTest(broughtTools2);
        
        magazineSetupScenario3 = new TestScenario();
        entry = new MachineOrderEntry();
        entry.setId("test0");
        entry.setLatenessAllowanceFactor(1.45f);
        entry.setMaxCost(2000);
        entry.setMaxTime(200);
        entry.setNcProgramNr("NC12072018");
        entry.setProjectedCost(1);
        entry.setProjectedSavings(0);
        entry.setProjectedTime(1);
        entry.setQuantity(10);
        entry.setProjectedUsedTools(new ToolRequirementList());
        entry.setSettingSheet(getSettingSheet("oa"));
        entry.setTooled(false);
        magazineSetupScenario3.magazineTools.add(new ToolInstance(drillD127, "1000", 0, 0, 0));
        magazineSetupScenario3.magazineTools.add(new ToolInstance(drillD13, "1001", 95, 0, 0));
        magazineSetupScenario3.magazineTools.add(new ToolInstance(shellMillD66, "1002", 33, 0, 0));
        magazineSetupScenario3.magazineTools.add(new ToolInstance(cylindricShaftMillD10, "1003", 0, 0, 0));
        magazineSetupScenario3.magazineTools.add(new ToolInstance(drillD127, "1004", 0, 0, 0));
        magazineSetupScenario3.magazineTools.add(new ToolInstance(drillD127, "1005", 0, 0, 0));
        magazineSetupScenario3.magazineTools.add(new ToolInstance(drillD127, "1006", 0, 0, 0));
        magazineSetupScenario3.magazineTools.add(new ToolInstance(drillD127, "1007", 0, 0, 0));
        magazineSetupScenario3.magazineTools.add(new ToolInstance(drillD127, "1008", 0, 0, 0));
        magazineSetupScenario3.magazineTools.add(new ToolInstance(drillD127, "1009", 0, 0, 0));
        magazineSetupScenario3.machineQueue.add(entry);
        ArrayList broughtTools3 = new ArrayList();
        broughtTools3.add(new ToolInstance(drillD13,"1011",0,0,0));
        broughtTools3.add(new ToolInstance(millingCutterD925,"1012",67,0,0));
        broughtTools3.add(new ToolInstance(millingCutterD925,"1013",67,0,0));
        broughtTools3.add(new ToolInstance(millingCutterD925,"1014",67,0,0));
        broughtTools3.add(new ToolInstance(millingCutterD925,"1015",67,0,0));
        broughtTools3.add(new ToolInstance(millingCutterD925,"1016",67,0,0));
        broughtTools3.add(new ToolInstance(millingCutterD925,"1017",67,0,0));
        broughtTools3.add(new ToolInstance(millingCutterD925,"1018",67,0,0));
        broughtTools3.add(new ToolInstance(millingCutterD925,"1019",67,0,0));
        magazineSetupScenario3.filledTestAction = new StartMagazineSetupTest(broughtTools3);
        
        buildUI();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == setEmptyScenarioBtn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(emptyScenario);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == setSystemTestScenarioBtn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(scenario1_2NoAction);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == setSystemTestScenario3Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(scenario3NoAction);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == setSystemTestScenario4Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(scenario4NoAction);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == emptyMagToolsBtn){
            GuiEvent event = new GuiEvent(this, 2);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == setMagToolsBtn){
            GuiEvent event = new GuiEvent(this, 3);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == scenario1_2SetupInfoTestBtn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(scenario1_2SetupInfoTest);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == setupInfoScenario1Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(setupInfoScenario1);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == setupInfoScenario2Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(setupInfoScenario2);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == setupInfoScenario3Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(setupInfoScenario3);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == setupInfoScenario4Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(setupInfoScenario4);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == setupInfoScenario5Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(setupInfoScenario5);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == toolRequisitionScenario1Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(toolRequisitionScenario1);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == toolRequisitionScenario2Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(toolRequisitionScenario2);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == magazineSetupScenario1Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(magazineSetupScenario1);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == magazineSetupScenario2Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(magazineSetupScenario2);
            myAgent.postGuiEvent(event);
        }else if(e.getSource() == magazineSetupScenario3Btn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(magazineSetupScenario3);
            myAgent.postGuiEvent(event);
        }else{
            //Do nothing!
        }
    }
    
    private void buildUI(){
        JPanel mainPanel = new JPanel(new GridLayout(0,2));
        add(mainPanel, BorderLayout.NORTH);
        
        setEmptyScenarioBtn = new JButton("Empty DB & Mag Tools");
        setSystemTestScenarioBtn = new JButton("System Test 1 Setup");
        setSystemTestScenario3Btn = new JButton("System Test 3 Setup");
        setSystemTestScenario4Btn = new JButton("System Test 4 Setup");
        emptyMagToolsBtn = new JButton("Empty Mag Tools");
        setMagToolsBtn = new JButton("Set Mag Tools");
        testMachineSetupInfoProtocolBtn = new JButton("Test Machine Setup Info Protocol");
        scenario1_2SetupInfoTestBtn = new JButton("Test Machine Setup Info Protocol (Setup 1)");
        
        setEmptyScenarioBtn.addActionListener(this);
        setSystemTestScenarioBtn.addActionListener(this);
        setSystemTestScenario3Btn.addActionListener(this);
        setSystemTestScenario4Btn.addActionListener(this);
        emptyMagToolsBtn.addActionListener(this);
        setMagToolsBtn.addActionListener(this);
        testMachineSetupInfoProtocolBtn.addActionListener(this);
        scenario1_2SetupInfoTestBtn.addActionListener(this);
        
        mainPanel.add(setEmptyScenarioBtn);
        mainPanel.add(setSystemTestScenarioBtn);
        mainPanel.add(setSystemTestScenario3Btn);
        mainPanel.add(setSystemTestScenario4Btn);
        
        setupInfoScenario1Btn = new JButton("Setup Info Scenario 1");
        setupInfoScenario1Btn.addActionListener(this);
        mainPanel.add(setupInfoScenario1Btn);
        
        setupInfoScenario2Btn = new JButton("Setup Info Scenario 2");
        setupInfoScenario2Btn.addActionListener(this);
        mainPanel.add(setupInfoScenario2Btn);
        
        setupInfoScenario3Btn = new JButton("Setup Info Scenario 3");
        setupInfoScenario3Btn.addActionListener(this);
        mainPanel.add(setupInfoScenario3Btn);
        
        setupInfoScenario4Btn = new JButton("Setup Info Scenario 4");
        setupInfoScenario4Btn.addActionListener(this);
        mainPanel.add(setupInfoScenario4Btn);
        
        setupInfoScenario5Btn = new JButton("Setup Info Scenario 5");
        setupInfoScenario5Btn.addActionListener(this);
        mainPanel.add(setupInfoScenario5Btn);
        
        toolRequisitionScenario1Btn = new JButton("Tool Requisition Scenario 1");
        toolRequisitionScenario1Btn.addActionListener(this);
        mainPanel.add(toolRequisitionScenario1Btn);
        
        toolRequisitionScenario2Btn = new JButton("Tool Requisition Scenario 2");
        toolRequisitionScenario2Btn.addActionListener(this);
        mainPanel.add(toolRequisitionScenario2Btn);
        
        magazineSetupScenario1Btn = new JButton("Magazine Setup Scenario 1");
        magazineSetupScenario1Btn.addActionListener(this);
        mainPanel.add(magazineSetupScenario1Btn);
        
        magazineSetupScenario2Btn = new JButton("Magazine Setup Scenario 2");
        magazineSetupScenario2Btn.addActionListener(this);
        mainPanel.add(magazineSetupScenario2Btn);
        
        magazineSetupScenario3Btn = new JButton("Magazine Setup Scenario 3");
        magazineSetupScenario3Btn.addActionListener(this);
        mainPanel.add(magazineSetupScenario3Btn);
    }
    private SettingSheet getSettingSheet(String ssid){
        SettingSheet ss = null;
        if(ssid.equals("oa")){
            ss = new SettingSheet("OA", "NC12072018", 4.74f, 3, new ToolRequirementList());
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(drillD13, 0.4f, 0, 74));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(cylindricShaftMillD10, 0.74f, 0, 79.5f));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(shellMillD66, 1.96f, 0, 81));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(millingCutterD925, 0.85f, 0, 68));
        }else if(ssid.equals("ob")){
            ss = new SettingSheet("OB", "NC12072019", 10.81f, 4, new ToolRequirementList());
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(drillD13, 0.85f, 0, 92));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(shellMillD66, 1.25f, 0, 89));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(millingCutterD3171, 1.45f, 0, 59));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(faceMillD60, 1.64f, 0, 80));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(threadMillM5X8, 0.86f, 0, 63));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(millingCutterD16, 1.34f, 0, 89));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(millingCutterD10, 1.62f, 0, 91));
        }else if(ssid.equals("oc")){
            ss = new SettingSheet("OC", "NC12072020", 6.23f, 2, new ToolRequirementList());
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(millingCutterD16, 0.89f, 0, 90));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(millingCutterD10, 0.58f, 0, 91));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(threadMillM5X8, 0.46f, 0, 61));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(countersinkD9, 0.16f, 0, 98));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(stirnfräserD60, 1.24f, 0, 74));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(faceMillD160, 0.78f, 0, 73));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(roundShankDrillD85, 0.64f, 0, 58.5f));
            ss.getToolRequirementsList().getToolRequirements().add(new ToolRequirementEntry(drillD127, 0.48f, 0, 57.5f));
        }
        return ss;
    }
}
