
package ontology.tool.terms;

import jade.content.AgentAction;

/**
 *  Command for a UI Agent to display a picking list
 *
 * @author Shahin Mahmody
 */
public class DisplayPickingList implements AgentAction{
    private PickingList pickingList;
    private int nextViableInvId;
    
    public DisplayPickingList(){
        super();
    }

    public DisplayPickingList(PickingList pickingList, int nextViableInvId) {
        this.pickingList = pickingList;
        this.nextViableInvId = nextViableInvId;
    }

    public int getNextViableInvId() {
        return nextViableInvId;
    }

    public void setNextViableInvId(int nextViableInvId) {
        this.nextViableInvId = nextViableInvId;
    }

    public PickingList getPickingList() {
        return pickingList;
    }

    public void setPickingList(PickingList pickingList) {
        this.pickingList = pickingList;
    }
}
