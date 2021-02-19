
package ontology.tool.terms;

import jade.content.Predicate;

/**
 * Provides setting sheet information for an NC programme number
 *
 * @author Shahin Mahmody
 */
public class NcProgramReferencesSheet implements Predicate{
    private String ncProgramNr;
    private SettingSheet settingSheet;
    
    public NcProgramReferencesSheet(){
        super();
    }

    public NcProgramReferencesSheet(String ncProgramNr, SettingSheet settingSheet) {
        this.ncProgramNr = ncProgramNr;
        this.settingSheet = settingSheet;
    }

    public String getNcProgramNr() {
        return ncProgramNr;
    }

    public void setNcProgramNr(String ncProgramNr) {
        this.ncProgramNr = ncProgramNr;
    }

    public SettingSheet getSettingSheet() {
        return settingSheet;
    }

    public void setSettingSheet(SettingSheet settingSheet) {
        this.settingSheet = settingSheet;
    }
    
    
}
