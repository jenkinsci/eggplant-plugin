package eggPlant;

import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import hudson.model.Result;
import java.io.DataInputStream;
import java.io.File;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newcommandLine(StaplerRequest)} is invoked
 * and a new {@link eggPlantBuilder} is created. The created
 * commandLine is persisted to the project configuration XML by using
 * XStream, so this allows you to use commandLine fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class eggPlantBuilder extends Builder {

    private final String commandLine;
    private final String script;
    private final String sut;
    private final String port;
    private final String password;
    private final String colorDepth;
    
    private final String globalResultsFolder;
    private final String defaultDocumentDirectory;
    private final String params;
    private final boolean reportFailures;
    private final boolean commandLineOutput;
    
    //operating system... it appears that Jenkins on Mac/Linux doesn't like quotes in the command
    private String operatingSystem;
    private String[] scripts; 

    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public eggPlantBuilder(String commandLine,
                           String script,
                           String SUT,
                           String port, 
                           String password, 
                           String colorDepth, 
                           String globalResultsFolder,
                           String defaultDocumentDirectory,
                           String params,
                           boolean reportFailures,
                           boolean commandLineOutput) {
        
        this.commandLine = commandLine;
        this.script = script;
        this.sut = SUT;
        this.port = port;
        this.password = password;
        this.colorDepth = colorDepth;
        this.globalResultsFolder = globalResultsFolder;
        this.defaultDocumentDirectory = defaultDocumentDirectory;
        this.params = params;
        this.reportFailures = reportFailures;
        this.commandLineOutput = commandLineOutput;
        
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public static String defaultCLI() {
        String operatingSystem = System.getProperty("os.name");
        if (operatingSystem.startsWith("Windows")) {
            return "C:\\Program Files\\Eggplant\\runscript.bat";
        } else if (operatingSystem.startsWith("Mac")) {
            return "/Applications/Eggplant/runscript";
        } else {
            return "/usr/local/bin/runscript";
        }
    }
    public String getCommandLine() {
        return commandLine;
    }

    public String getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getSUT() {
        return sut;
    }

    public String getScript() {
        return script;
    }

    public String getColorDepth() {
        return colorDepth;
    }
    
    //ajf 10/26/11 added getter for default doc dir
    
    public String getDefaultDocumentDirectory(){
        return defaultDocumentDirectory;
    }
    
    //ajf 10/18/11: may Need to do some OS-Specific things when building the runscript path...
    public String getOS() {
        if (operatingSystem == null) {
            operatingSystem = System.getProperty("os.name");
        }
        return operatingSystem;
    }
    
    public boolean isWindows() {
        return getOS().startsWith("Windows");
    }
    
    public boolean isMac() {
        return getOS().startsWith("Mac");
    }
    
    public boolean isLinux() {
        //@TODO: this needs to be checked before it's used
        return getOS().startsWith("Linux");
    }

    /**
     * This is where you 'build' the project. 
     * This runs eggPlant from the command line and then reads in the results
     * so that Jenkins can display them
     * 
     * @param build
     * @param launcher
     * @param listener
     * @return 
     */
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        //  Start logging - this will appear in the console output
        listener.getLogger().println("eggPlant execution started");

        String epWorkingDir;
        String workspacedir;
        String ls_str;
        
        //ajf 10/21/11: rewrote in a more systemmatic fashion to fix 20016287
        //vars
        String assetsDir; //directory where eggPlant scripts exist

        try {
                       
            //Construct the command line execution
            RunscriptCommand runner = new RunscriptCommand(build,commandLine,script);
            
            //Step 1: path to runscript--handled by the RunscriptCommand Object
            listener.getLogger().println("Command Step 1 - Runscript Location: " + runner.getRunscript());
            
            //Step 2: Construct the list of scrips - handled by the runscript command object
            listener.getLogger().println("Command Step 2 - Scripts to Run: " + runner.getScripts());

            
            //Step 3: other command line flags
            if (!sut.isEmpty())      runner.setHost(sut);
            if (!port.isEmpty())     runner.setPort(Integer.parseInt(port));
            if (!password.isEmpty()) runner.setPassword(password);
            if (!colorDepth.isEmpty()) runner.setColorDepth(Integer.parseInt(colorDepth));
            if (!defaultDocumentDirectory.isEmpty()) runner.setDefaultDocumentDirectory(defaultDocumentDirectory);
            if (reportFailures) runner.setReportFailures(reportFailures);
            if (commandLineOutput) runner.setCommandLineOutput(commandLineOutput);
            if (!params.isEmpty()) runner.setParams(params);

            if (globalResultsFolder.isEmpty()){
                epWorkingDir = build.getRootDir().getAbsolutePath();
                runner.setGlobalResultsFolder(epWorkingDir);
            } 
            else {
               runner.setGlobalResultsFolder(globalResultsFolder);
            }
            
            listener.getLogger().println("Command Step 3 - Other Command Line Flags,"
                    +"Host: "+runner.getHost()
                    +", Port: "+runner.getPort()
                    +", Password: "+runner.getPassword()
                    +", Color Depth: "+runner.getColorDepth()
                    +", Global Results Folder: "+runner.getGlobalResultsFolder()
                    +", Default Document Directory: "+runner.getDefaultDocumentDirectory());
            
            //step 4: put the runscript command together
            runner.buildRunscriptCommandString();
            
            listener.getLogger().println("Fully Constructed Runscript Command: "
                    +runner.getRunScriptCommandString());
            
            //  runner object will be run from the command line
            listener.getLogger().println("Now Executing" + 
                    runner.getRunScriptCommandString()+"...");
            
            //JN: executes ONLY on the same machine as Jenkins
                Process ls_proc = Runtime.getRuntime().exec(runner.getRunScriptCommandString());
            
            //  TODO: Distributed executions
            //Something like this will run on a distributed machine
            //launcher.launch().cmds(runner).stdout(listener.getLogger());  
            //.envs(build.getEnvironment(listener)).stdout(listener.getLogger()).pwd(build.getWorkspace()).join();

            // get its output (your input) stream 
            DataInputStream ls_in = new DataInputStream(ls_proc.getInputStream());

            try {
                while ((ls_str = ls_in.readLine()) != null) {
                    listener.getLogger().println(ls_str);
                }
            } catch (Exception e) {
                build.setResult(Result.UNSTABLE);
                listener.getLogger().println("Exception: " + e.getMessage());
            }
        } catch (Exception e) {
            listener.getLogger().println("Exception: " + e.getMessage());
            build.setResult(Result.UNSTABLE);
        }

        listener.getLogger().println("Runscript execution completed");
        listener.getLogger().println("Parsing results...");

        eggPlantAction action = build.getAction(eggPlantAction.class);

        if (action == null) {
            action = new eggPlantAction(build);
            build.addAction(action);
        }
        
        EggplantParser parser = new EggplantParser();
        scripts = script.split(",");
        
        //  Get the files in the build job directory
        File[] files = build.getRootDir().listFiles();
        boolean passed = true;
        int j = 0;//script counter
        //  Loop round all the files
        for (int i = 0; i < files.length; i++)
        {
            File file = files[i];
            //  Only interested in directories
            if (file.isDirectory())
            {
                //  Read the results out of the directory
                //  If there is no RunHistory.csv
                //  then nothing will be logged
                String startDir = file.getAbsolutePath();
                listener.getLogger().println("Results Dir: " + startDir);
                passed &= parser.parseResult(action, startDir, build.getUrl(),scripts[j],sut);
                j++;
            }
        }
        
        //  eggPlant has finished
        listener.getLogger().println("eggPlant parsing results finished");

        //  Return the result of the eggPlant bit
        return passed;
    }
    
    /**
     * This bit is for the global settings of Jenkins
     * It allows global settings for eggPlant
     * TODO: Does not work
     */

    /**
     * Descriptor for {@link eggPlantBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String _commandLine;

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set a name");
            }
            if (value.length() < 4) {
                return FormValidation.warning("Isn't the name too short?");
            }
            return FormValidation.ok();
        }
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "eggPlant Test";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            _commandLine = formData.getString("epcommandLine");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req, formData);
        }

        public String epcommandLine() {
            return _commandLine;
        }
    }
}
