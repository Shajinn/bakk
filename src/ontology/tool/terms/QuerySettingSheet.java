
package ontology.tool.terms;

import jade.content.AgentAction;

/**
 * Request setting sheet details for an NC programme
 *
 * @author Shahin Mahmody
 */
public class QuerySettingSheet implements AgentAction{
    private String ncProgramNr;
    
    public QuerySettingSheet() {
    }

    public QuerySettingSheet(String ncProgramNr) {
        this.ncProgramNr = ncProgramNr;
    }

    public String getNcProgramNr() {
        return ncProgramNr;
    }

    public void setNcProgramNr(String ncProgramNr) {
        this.ncProgramNr = ncProgramNr;
    }
    
}
