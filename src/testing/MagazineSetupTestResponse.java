
package testing;

import jade.content.Concept;
import jade.content.Predicate;
import jade.util.leap.List;

/**
 *
 * @author Shahin Mahmody
 */
public class MagazineSetupTestResponse implements Predicate{
    private boolean tooManyTools = false;
    private boolean tooLittleLifetime = false;
    private List magazine;

    public MagazineSetupTestResponse() {
    }

    public MagazineSetupTestResponse(List magazine) {
        this.magazine = magazine;
    }

    public boolean getTooManyTools() {
        return tooManyTools;
    }

    public void setTooManyTools(boolean tooManyTools) {
        this.tooManyTools = tooManyTools;
    }

    public boolean getTooLittleLifetime() {
        return tooLittleLifetime;
    }

    public void setTooLittleLifetime(boolean tooLittleLifetime) {
        this.tooLittleLifetime = tooLittleLifetime;
    }

    public List getMagazine() {
        return magazine;
    }

    public void setMagazine(List magazine) {
        this.magazine = magazine;
    }
    
    
}
