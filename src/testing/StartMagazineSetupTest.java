
package testing;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 *
 * @author Shahin Mahmody
 */
public class StartMagazineSetupTest implements AgentAction{
    private List tools;
    
    public StartMagazineSetupTest(){
        this.tools = new ArrayList();
    }

    public StartMagazineSetupTest(List tools) {
        this.tools = tools;
    }

    public List getTools() {
        return tools;
    }

    public void setTools(List tools) {
        this.tools = tools;
    }
    
    
}
