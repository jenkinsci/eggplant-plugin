/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggPlant;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author testplant
 * This stores the results of an eggPlant execution
 */
public class eggPlantResult implements Serializable {
    /** serialVersionUID */
    private static final long serialVersionUID = -7436928706318596861L;

    /** script */
    private String script;
    
    /** sut */
    private String sut;

    /** isPassed */
    private boolean isPassed;

    /** runDate */
    private String runDate;

    /** Duration */
    private String Duration;

    /** errors */
    private String errors;
    
    /** warnings */
    private String warnings;
    
    /** exceptions */
    private String  exceptions;

    /** ScriptLines */
    private ArrayList<eggPlantScriptLine> ScriptLines;

    public String getSut() {
        return sut;
    }

    public void setSut(String sut) {
        this.sut = sut;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String Duration) {
        this.Duration = Duration;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getExceptions() {
        return exceptions;
    }

    public void setExceptions(String exceptions) {
        this.exceptions = exceptions;
    }

    public String getRunDate() {
        return runDate;
    }

    public void setRunDate(String runDate) {
        this.runDate = runDate;
    }

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }


    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    /**
     * @return isPassed
     */
    public boolean isPassed() {
        return isPassed;
    }
    
    public void setPassed(boolean isPassed) {
        this.isPassed = isPassed;
    }

    public void addScriptLine(eggPlantScriptLine esl)
    {
        if (ScriptLines == null)
        {
            ScriptLines = new ArrayList<eggPlantScriptLine>();
        }
        ScriptLines.add(esl);
    }

    /**
     * @return ScriptLines
     */
    public ArrayList<eggPlantScriptLine> getScriptLines() {
        return ScriptLines;
    }

    /**
     * @param scriptlines ScriptLines
     */
    public void setScriptLines(ArrayList<eggPlantScriptLine> scriptlines) {
        this.ScriptLines = scriptlines;
    }

}
