package eggPlant;

import hudson.model.AbstractBuild;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/** This class contains data and methods for constructing the runscript command
 *  that will be passed to Jenkins for execution.
 *
 * @author afisher
 * @version 1.0
 */
public class RunscriptCommand {

    //runscript parameters
    private String runscript;
    private String scripts;
    private String host;
    private int port;
    private String password;
    private int colorDepth;
    private int repeatCount;
    private String globalResultsFolder;
    private String defaultDocumentDirectory;
    private boolean commandLineOutput;
    private boolean reportFailures;
    private String params;
    private AbstractBuild build;
    private String runscriptCommandString; //final constructed runscript command
    ArrayList<String> cmdList = new ArrayList<String>();
    
    public RunscriptCommand(AbstractBuild b, 
                            String exeLoc, 
                            String scriptList){
        runscript = exeLoc;
        build = b;
        colorDepth = 0;
        repeatCount = 0;
        port = 0;
        scripts = "";
        params = "";
        
        String [] scriptArray = scriptList.split(",");
        
//        for (int i = 0; i < scriptArray.length; i++){
//                scripts += "\""+build.getWorkspace()
//                        +File.separator
//                        +scriptArray[i].trim()+"\" ";  
//        }
        
         for (int i = 0; i < scriptArray.length; i++){
                scripts += build.getWorkspace()
                        +File.separator
                        +scriptArray[i].trim()+" ";  
        }
        
        scripts = scripts.trim();
        runscriptCommandString =  runscript+" "+scripts;
        
    }
    
    public void buildRunscriptCommandString(){
        
        if (host != null) runscriptCommandString += " -host " + host;
        if (port != 0) runscriptCommandString += " -port " + port;
        if (password != null) runscriptCommandString += " -password " + password;
        if (colorDepth != 0) runscriptCommandString += " -colorDepth " + colorDepth;
        if (repeatCount != 0) runscriptCommandString += " -repeat " + repeatCount;
        if (globalResultsFolder != null) runscriptCommandString += " -GlobalResultsFolder " + globalResultsFolder;
        if (defaultDocumentDirectory != null) runscriptCommandString += " -DefaultDocumentDirectory " + defaultDocumentDirectory;
        if (commandLineOutput == true) runscriptCommandString += " -CommandLineOutput YES ";
        if (reportFailures == true) runscriptCommandString += " -ReportFailures YES ";
        
        //params
        if (!params.isEmpty()) {
            runscriptCommandString += " -params "+params;
        } 

    }
      
    public void buildRunscriptCommandList(){
        
        if (runscript != null) {
            cmdList.add(runscript);
        }
        if (scripts != null) {
            String[] myScriptArray = scripts.split(" ");
            cmdList.addAll(Arrays.asList(myScriptArray));
        }
        if (host != null) {
            cmdList.add("-host");
            cmdList.add(host);
        }
        if (port != 0) {
            cmdList.add("-port");
            cmdList.add(Integer.toString(port));
        }
        if (password != null) {
            cmdList.add("-password");
            cmdList.add(password);
        }
        if (colorDepth != 0) {
            cmdList.add("-colorDepth");
            cmdList.add(Integer.toString(colorDepth));
        }
        if (repeatCount != 0) {
            cmdList.add("-repeat");
            cmdList.add(Integer.toString(repeatCount));
        }
        if (globalResultsFolder != null) {
            cmdList.add("-GlobalResultsFolder");
            cmdList.add(globalResultsFolder);
        }
        if (defaultDocumentDirectory != null) {
            cmdList.add("-DefaultDocumentDirectory");
            cmdList.add(defaultDocumentDirectory);
        }
        if (commandLineOutput == true) {
            cmdList.add("-CommandLineOutput");
            cmdList.add("YES");
        }
        if (reportFailures == true) {
            cmdList.add("-ReportFailures");
            cmdList.add("YES");
        }
        
        //params
        if (!params.isEmpty()) {
            cmdList.add("-params");
            cmdList.add(params);
        } 
        
        
    }
    
    public String getRunScriptCommandString(){
        return runscriptCommandString;
    }
    
    public ArrayList<String> getRunscriptCommandList() {
        return cmdList;
    }

    public int getColorDepth() {
        return colorDepth;
    }

    public void setColorDepth(int colorDepth) {
        this.colorDepth = colorDepth;
    }

    public boolean isCommandLineOutput() {
        return commandLineOutput;
    }

    public void setCommandLineOutput(boolean commandLineOutput) {
        this.commandLineOutput = commandLineOutput;
    }

    public String getDefaultDocumentDirectory() {
        return defaultDocumentDirectory;
    }

    public void setDefaultDocumentDirectory(String defaultDocumentDirectory) {
        this.defaultDocumentDirectory = defaultDocumentDirectory;
    }

    public String getGlobalResultsFolder() {
        return globalResultsFolder;
    }

    public void setGlobalResultsFolder(String globalResultsFolder) {
        this.globalResultsFolder = globalResultsFolder;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String prms) {
        String[] parameters = prms.split(",");

        for (int i = 0; i < parameters.length; i++) {
            params += parameters[i] + " ";
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public boolean isReportFailures() {
        return reportFailures;
    }

    public void setReportFailures(boolean reportFailures) {
        this.reportFailures = reportFailures;
    }

    public String getRunscript() {
        return runscript;
    }

    public void setRunscript(String runscript) {
        this.runscript = runscript;
    }

    public String getScripts() {
        return scripts;
    }

    public void setScripts(String scripts) {
        this.scripts = scripts;
    }
}
