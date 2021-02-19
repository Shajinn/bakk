
package testing;

import jade.content.AgentAction;

/**
 *
 * @author Shahin Mahmody
 */
public class StartToolRequisitionTest implements AgentAction {
    private int position;

    public StartToolRequisitionTest(int position) {
        this.position = position;
    }

    public StartToolRequisitionTest() {
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    
}
