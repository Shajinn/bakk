
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
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import ontology.tool.terms.ToolInstance;

/**
 * UI showing tools heading back to storage
 *
 * @author Shahin Mahmody
 */
public class ToolsForStorageGUI extends JFrame implements ActionListener{
    private final VisualAgent myAgent;
    private final List toolsForStorage;
    private final ACLMessage messageShell;
    
    private JButton okBtn;

    public ToolsForStorageGUI(VisualAgent myAgent, List toolsForStorage, ACLMessage messageShell){
        this.myAgent = myAgent;
        this.toolsForStorage = toolsForStorage;
        this.messageShell = messageShell;
        
        buildUI();
    }
    
    private void buildUI(){
        if(toolsForStorage == null || toolsForStorage.isEmpty()){
            JPanel msgPanel = new JPanel();
            msgPanel.add(new Label("No tools are going back to storage!"));
            add(msgPanel, BorderLayout.NORTH);
        }else{
            add(getToolsPanel(toolsForStorage), BorderLayout.NORTH);
        }
        
        okBtn = new JButton("OK");
        add(okBtn,BorderLayout.SOUTH);
        okBtn.addActionListener(this);
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    private JPanel getToolsPanel(List tools){
        JPanel toolsPanel = new JPanel(new GridLayout(0,1));
        if(tools != null){
            HashMap<String,ArrayList<ToolInstance>> toolsMap = new HashMap<String,ArrayList<ToolInstance>>();
            for(Iterator toolIterator = tools.iterator();toolIterator.hasNext(); ){
                ToolInstance toolInstance = (ToolInstance)toolIterator.next();
                toolsMap.putIfAbsent(toolInstance.getTool().getToolId(), new ArrayList<ToolInstance>());
                toolsMap.get(toolInstance.getTool().getToolId()).add(toolInstance);
            }
            
            for(String toolId: toolsMap.keySet()){
                ArrayList<ToolInstance> toolsList = toolsMap.get(toolId);
                JPanel toolInstancePanel = new JPanel(new GridLayout(0,1));
                toolsPanel.add(toolInstancePanel,BorderLayout.NORTH);
                toolInstancePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
                toolInstancePanel.add(new Label(toolsList.get(0).getTool().getName()));
                
                toolInstancePanel.add(new JSeparator());
                
                for(ToolInstance ti: toolsList){
                    toolInstancePanel.add(new Label(ti.getInstanceId()));
                }
            }
        }
        
        return toolsPanel;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == okBtn){
            GuiEvent event = new GuiEvent(this, 0);
            event.addParameter(messageShell);
            event.addParameter(toolsForStorage);
            myAgent.postGuiEvent(event);
        }else{
            //Do nothing!
        }
    }
}
