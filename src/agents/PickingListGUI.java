
package agents;

import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import ontology.tool.terms.PickingListEntry;
import ontology.tool.terms.ToolInstance;

/**
 * A UI containing newly assembled tools and their components. Allows naming of new tools
 *
 * @author Shahin Mahmody
 */
public class PickingListGUI extends JFrame implements ActionListener{
    public static final String TOOLS_PARAM = "tools";
    
    private final List assembledTools;
    private final ArrayList namedTools;
    private final HashMap<String, java.util.ArrayList<ToolInstance>> assembledToolsMap;
    private final HashMap<String, java.util.ArrayList<PickingListEntry>> componentsMap;
    private final HashMap<String, java.util.ArrayList<TextField>> textFieldsMap;
    private final ACLMessage messageShell;
    private final VisualAgent myAgent;
    
    private JButton okBtn;

    public PickingListGUI(VisualAgent myAgent, List assembledTools, List components,ACLMessage originMessage){
        this.myAgent = myAgent;
        this.assembledTools = assembledTools;
        this.messageShell = originMessage;
        namedTools = new ArrayList();
        
        if(assembledTools == null){
            assembledTools = new ArrayList();
        }
        if(components == null){
            components = new ArrayList();
        }
        
        assembledToolsMap = new HashMap<String, java.util.ArrayList<ToolInstance>>();
        Iterator toolsIterator = assembledTools.iterator();
        while(toolsIterator.hasNext()){
            ToolInstance instance = (ToolInstance)toolsIterator.next();
            assembledToolsMap.putIfAbsent(instance.getTool().getToolId(), new java.util.ArrayList<ToolInstance>());
            assembledToolsMap.get(instance.getTool().getToolId()).add(instance);
        }
        
        componentsMap = new HashMap<String, java.util.ArrayList<PickingListEntry>>();
        Iterator componentsIterator = components.iterator();
        while(componentsIterator.hasNext()){
            PickingListEntry component = (PickingListEntry)componentsIterator.next();
            componentsMap.putIfAbsent(component.getTool().getToolId(), new java.util.ArrayList<PickingListEntry>());
            componentsMap.get(component.getTool().getToolId()).add(component);
        }
        
        
        textFieldsMap = new HashMap<String, java.util.ArrayList<TextField>>();
        buildUI();
    }
    
    private void buildUI(){
        
        if(assembledToolsMap.keySet().isEmpty()){
            JPanel msgPanel = new JPanel();
            msgPanel.add(new Label("There are no new tools to assemble!"));
            add(msgPanel, BorderLayout.NORTH);
        }else{
            JPanel toolsPanel = new JPanel(new GridLayout(0,1));
            add(toolsPanel,BorderLayout.NORTH);
            for(String toolId: assembledToolsMap.keySet()){
                if(componentsMap.containsKey(toolId)){
                    toolsPanel.add(getToolAssemblyPanel(assembledToolsMap.get(toolId),componentsMap.get(toolId)));
                }
            }
        }
        okBtn = new JButton("OK");
        
        add(okBtn,BorderLayout.SOUTH);
        okBtn.addActionListener(this);
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == okBtn){
            GuiEvent event = new GuiEvent(this, 0);
            for(String toolId: assembledToolsMap.keySet()){
                java.util.ArrayList<ToolInstance> toolInstanceList = assembledToolsMap.get(toolId);
                java.util.ArrayList<TextField> textFieldsList = textFieldsMap.get(toolId);
                for(int i = 0; i < toolInstanceList.size(); i++){
                    ToolInstance tool = toolInstanceList.get(i);
                    tool.setInstanceId(textFieldsList.get(i).getText());
                    namedTools.add(tool);
                }
            }
            event.addParameter(messageShell);
            event.addParameter(namedTools);
            myAgent.postGuiEvent(event);
        }else{
            //Do nothing!
        }
    }
    
    private JPanel getToolAssemblyPanel(java.util.ArrayList<ToolInstance> assembledInstances,
            java.util.ArrayList<PickingListEntry> components){
        JPanel assemblyPanel = new JPanel(new GridLayout(0,2));
        assemblyPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        if(!assembledInstances.isEmpty()){
            textFieldsMap.put(assembledInstances.get(0).getTool().getToolId(), new java.util.ArrayList<TextField>());
            assemblyPanel.add(new Label(assembledInstances.get(0).getTool().getName()));
            assemblyPanel.add(new Label("x" + assembledInstances.size()));
            assemblyPanel.add(new JSeparator());
            assemblyPanel.add(new JSeparator());
            for(PickingListEntry component: components){
                assemblyPanel.add(new Label(component.getComponent().getName()));
                assemblyPanel.add(new Label(component.getComponent().getDescription()));
            }
            assemblyPanel.add(new JSeparator());
            assemblyPanel.add(new JSeparator());
            for(int i = 0; i < assembledInstances.size(); i++){
                assemblyPanel.add(new Label("Instance#" + i));
                TextField tf = new TextField();
                tf.setName(assembledInstances.get(0).getTool().getToolId()+"_"+i);
                tf.setText(assembledInstances.get(i).getInstanceId());
                assemblyPanel.add(tf);
                
                textFieldsMap.get(assembledInstances.get(0).getTool().getToolId()).add(tf);
            }
            assemblyPanel.add(new JSeparator());
            assemblyPanel.add(new JSeparator());
        }
        return assemblyPanel;
    }
    
    public List getAssembledTools(){
        return assembledTools;
    }
}
