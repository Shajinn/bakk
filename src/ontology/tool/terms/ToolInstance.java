
package ontology.tool.terms;

import jade.content.Concept;

/**
 * Concrete machine tool
 *
 * @author Shahin Mahmody
 */
public class ToolInstance implements Concept{
    private Tool tool;
    private String instanceId;
    private float usedTime;
    private float lengthDeviation;
    private float radiusDeviation;

    public ToolInstance() {
        super();
    }
    
    public ToolInstance(Tool tool, String instanceId, float usedTime, float lengthDeviation, float radiusDeviation) {
        this.tool = tool;
        this.instanceId = instanceId;
        this.usedTime = usedTime;
        this.lengthDeviation = lengthDeviation;
        this.radiusDeviation = radiusDeviation;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public float getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(float usedTime) {
        this.usedTime = usedTime;
    }

    public float getLengthDeviation() {
        return lengthDeviation;
    }

    public void setLengthDeviation(float lengthDeviation) {
        this.lengthDeviation = lengthDeviation;
    }

    public float getRadiusDeviation() {
        return radiusDeviation;
    }

    public void setRadiusDeviation(float radiusDeviation) {
        this.radiusDeviation = radiusDeviation;
    }   
}
