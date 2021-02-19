
package agents;

import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import ontology.tool.terms.ToolInstance;

/**
 * A simple UI to inspect an incoming commission list's tools and see measurements
 *
 * @author Shahin Mahmody
 */
public class CommissionListGUI extends JFrame implements ActionListener{
    private final List measuredTools;
    private final ACLMessage messageShell;
    private final VisualAgent myAgent;
    
    private JPanel container;  
    private JButton okBtn;
    
    public CommissionListGUI(VisualAgent myAgent, List measuredTools, ACLMessage messageShell){
        this.measuredTools = measuredTools;
        this.myAgent = myAgent;
        this.messageShell = messageShell;
        
        buildUI();
    }
    
    private void buildUI(){
         container = new JPanel();
        JScrollPane scrollPane = new JScrollPane(container);
        add(scrollPane,BorderLayout.NORTH);
        if(measuredTools == null || measuredTools.isEmpty()){
            JPanel msgPanel = new JPanel();
            msgPanel.add(new Label("No tools need new measurements!"));
            container.add(msgPanel);
        }else{
            container.add(getMeasurementsPanel(measuredTools));
        }
        
        okBtn = new JButton("OK");
        add(okBtn, BorderLayout.SOUTH);
        okBtn.addActionListener(this);
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    private JPanel getMeasurementsPanel(List tools){
        JPanel measurementsPanel = new JPanel(new GridLayout(0,1));
        if(measuredTools != null){
            HashMap<String,ArrayList<ToolInstance>> toolsMap = new HashMap<String,ArrayList<ToolInstance>>();
            for(Iterator toolIterator = tools.iterator();toolIterator.hasNext(); ){
                ToolInstance toolInstance = (ToolInstance)toolIterator.next();
                toolsMap.putIfAbsent(toolInstance.getTool().getToolId(), new ArrayList<ToolInstance>());
                toolsMap.get(toolInstance.getTool().getToolId()).add(toolInstance);
            }
            
            for(String toolId: toolsMap.keySet()){
                ArrayList<ToolInstance> toolsList = toolsMap.get(toolId);
                JPanel toolInstancePanel = new JPanel(new GridLayout(0,3));
                measurementsPanel.add(toolInstancePanel,BorderLayout.NORTH);
                toolInstancePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
                toolInstancePanel.add(new Label(toolsList.get(0).getTool().getName()));
                toolInstancePanel.add(new Label("Length deviation"));
                toolInstancePanel.add(new Label("Radius deviation"));
                
                toolInstancePanel.add(new JSeparator());
                toolInstancePanel.add(new JSeparator());
                toolInstancePanel.add(new JSeparator());
                
                for(ToolInstance ti: toolsList){
                    toolInstancePanel.add(new Label(ti.getInstanceId()));
                    toolInstancePanel.add(new Label(Float.toString(ti.getLengthDeviation())));
                    toolInstancePanel.add(new Label(Float.toString(ti.getRadiusDeviation())));
                }
            }
        }
        
        return measurementsPanel;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == okBtn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(messageShell);
            event.addParameter(measuredTools);
            myAgent.postGuiEvent(event);
        }else{
            //Do nothing!
        }
    }

}
