
package ontology.tool.terms;

import jade.content.Predicate;

/**
 * A picking list generated for a list of tools
 *
 * @author Shahin Mahmody
 */
public class GeneratedPickingList implements Predicate{
    private PickingList pickingList;
    private int nextViableInvId;

    public GeneratedPickingList(){
        super();
    }

    public GeneratedPickingList(PickingList pickingList, int nextViableInvId) {
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
