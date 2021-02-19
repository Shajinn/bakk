
package ontology.tool.terms;

import jade.content.Concept;

/**
 * A component part of a tool to be assembled
 *
 * @author Shahin Mahmody
 */
public class Component implements Concept{
    private String componentId;
    private String name;
    private String description;
    private String toolId;

    public Component() {
        super();
    }

    public Component(String componentId, String name, String description, String toolId) {
        this.componentId = componentId;
        this.name = name;
        this.description = description;
        this.toolId = toolId;
    }
    
    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
    
}
