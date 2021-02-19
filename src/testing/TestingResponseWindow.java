
package testing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Shahin Mahmody
 */
public class TestingResponseWindow extends JFrame implements ActionListener{
    
    public TestingResponseWindow(String message){
        JPanel mainPanel = new JPanel();
        add(mainPanel, BorderLayout.NORTH);
        JTextArea text = new JTextArea(message,30,40);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        //text.setSize(400, 700);
        JScrollPane scroll = new JScrollPane(text);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scroll);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
}
