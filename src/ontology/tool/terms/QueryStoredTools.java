
package ontology.tool.terms;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Request information on already assembled tools
 *
 * @author Shahin Mahmody
 */
public class QueryStoredTools implements AgentAction{
    private List tools;
    
    public QueryStoredTools(){
        super();
    }

    public QueryStoredTools(List tools) {
        this.tools = tools;
    }

    public List getTools() {
        return (tools == null) ? tools = new ArrayList() : tools;
    }

    public void setTools(List tools) {
        this.tools = tools;
    }

}
