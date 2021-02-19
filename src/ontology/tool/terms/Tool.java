
package ontology.tool.terms;

import jade.content.Concept;
import java.util.Objects;

/**
 * Machine tool type reference data
 *
 * @author Shahin Mahmody
 */
public class Tool implements Concept{
    private String toolId;
    private float lifetime;
    private float criticaltime;
    private String name; 
    
    public Tool() {
        super();
    }

    public Tool(String toolId, float lifetime, float criticaltime, String name) {
        this.toolId = toolId;
        this.lifetime = lifetime;
        this.criticaltime = criticaltime;
        this.name = name;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public float getLifetime() {
        return lifetime;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public float getCriticaltime() {
        return criticaltime;
    }

    public void setCriticaltime(float criticaltime) {
        this.criticaltime = criticaltime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.toolId);
        hash = 37 * hash + Float.floatToIntBits(this.lifetime);
        hash = 37 * hash + Float.floatToIntBits(this.criticaltime);
        hash = 37 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tool other = (Tool) obj;
        if (Float.floatToIntBits(this.lifetime) != Float.floatToIntBits(other.lifetime)) {
            return false;
        }
        if (Float.floatToIntBits(this.criticaltime) != Float.floatToIntBits(other.criticaltime)) {
            return false;
        }
        if (!Objects.equals(this.toolId, other.toolId)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
    
    
}
