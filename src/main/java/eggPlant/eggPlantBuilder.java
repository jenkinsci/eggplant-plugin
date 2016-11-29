package eggPlant;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

public class eggPlantBuilder extends Builder {

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
    private final String installationName;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public eggPlantBuilder(String script, String SUT, String port, String password,
            String colorDepth, String globalResultsFolder, String defaultDocumentDirectory,
            String params, boolean reportFailures, boolean commandLineOutput,
            String installationName)
    {
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
        this.installationName = installationName;
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

    public boolean getCommandLineOutput() {
        return commandLineOutput;
    }
    public boolean getReportFailures() {
        return reportFailures;
    }

    public String getDefaultDocumentDirectory ()
    {
        return defaultDocumentDirectory;
    }

    public String getInstallationName() {
        return installationName;
    }

    public String getGlobalResultsFolder() {
        return globalResultsFolder;
    }

    public String getParams() {
        return params;
    }


    private String getResolvedString(String str, AbstractBuild build, BuildListener listener) throws IOException, InterruptedException
    {
        if (str == null) {
            return null;
        }

        String resolvedStr;

        resolvedStr = Util.replaceMacro(str, build.getEnvironment(listener));
        resolvedStr = Util.replaceMacro(resolvedStr, build.getBuildVariables());
        return resolvedStr;
    }


    EggPlantInstallation getInstallation(AbstractBuild<?,?> build, BuildListener listener) throws IOException, InterruptedException
    {
        for (final EggPlantInstallation i : getDescriptor().getInstallations())
        {
            if (installationName != null && installationName.equals(i.getName()))
            {
                return (EggPlantInstallation)i.translate(build, listener);
            }
        }
        return null;
    }

    /**
     * This is where the build step is executed. This runs eggPlant from the
     * command line and then reads in the results so that Jenkins can display
     * them
     *
     * @param build
     * @param launcher
     * @param listener
     * @return
     */
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException
    {
        //  Start logging - this will appear in the console output
        final PrintStream log = listener.getLogger();

        if (script == null || Util.fixEmptyAndTrim(script) == null)
        {
            log.println("Script name required for eggPlant execution.");
            return false;
        }

        log.println("eggPlant execution started");

        final EggPlantInstallation installation = getInstallation(build, listener);
        if (installation == null)
        {
             log.println("eggPlant installation not found for this node.");
             return false;
        }

        final String commandLine = installation.getHome();
        if (commandLine == null || commandLine.isEmpty()) {
             log.println("eggPlant runtime not defined.");
             return false;
        }

        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(installation.getHome());

        try {
            prepArgs(args, build, listener);

            // run script
            final int returnVal = launcher.launch()
                    .cmds(args)
                    .stdout(listener)
                    .pwd(build.getModuleRoot())
                    .join();

            if (returnVal != 0) {
                listener.finished(Result.FAILURE);
            }

            log.println("Runscript execution completed");

            // Begin log parsing
            log.println("Parsing results...");

            eggPlantAction action = build.getAction(eggPlantAction.class);

            if (action == null) {
                action = new eggPlantAction(build);
                build.addAction(action);
            }

            final FilePath [] results = build.getWorkspace().list("**/RunHistory.csv");

            for (final FilePath fp : results)
            {
                //  Read the results out of the directory
                //  If there is no RunHistory.csv, nothing will be logged

                log.println("Parsing results for test: " + fp.getParent().getName());

                final List<eggPlantResult> rList = fp.act(new EggplantParser(sut, build.getUrl()));
                action.getResultList().addAll(rList);
            }

            //  eggPlant has finished
            log.println("Finished parsing eggPlant results");

            //  Return the result of the eggPlant bit
            final List<eggPlantResult> resultsList=  action.getResultList();
            for (final eggPlantResult result: resultsList)
            {
                if (!result.isPassed())
                {
                    return false;
                }
            }

            return true;

        } catch (final IOException ex) {
            log.println(ex.getMessage());
            return false;
        } catch (final InterruptedException ex) {
            log.println(ex.getMessage());
            return false;
        }
    }

    private void prepArgs(ArgumentListBuilder args, AbstractBuild build, BuildListener listener) throws IOException, InterruptedException
    {
        if (script.indexOf(',') > 0)
        {
            final String [] scriptArray = script.split(",");
            for (final String scr: scriptArray)
            {
                args.add(getResolvedString(scr, build, listener));
            }
        }
        else
        {
            args.addTokenized(getResolvedString(script, build, listener));
        }

        if (!sut.isEmpty()) {
            args.add("-host");
            args.add(getResolvedString(sut, build, listener));
        }

        if (!port.isEmpty()) {
            args.add("-port");
            args.add(getResolvedString(port, build, listener));
        }
        if (!password.isEmpty()) {
            args.add("-password");
            args.add(password);
        }
        if (!colorDepth.isEmpty()) {
            args.add("-colorDepth");
            args.add(colorDepth);
        }

        if (reportFailures) {
            args.add("-ReportFailures");
            args.add("YES");
        }
        if (commandLineOutput) {
            args.add("-CommandLineOutput");
            args.add("YES");
        }
        if (!params.isEmpty()) {
            if (params.indexOf(',') > 0)
            {
                final String[] parameters = params.split(",");
                for (int i = 0; i < parameters.length; i++) {
                    args.add(getResolvedString(parameters[i], build, listener));
                }
            }
            else
            {
                args.addTokenized(getResolvedString(params, build, listener));
            }
        }

        args.add("-DefaultDocumentDirectory");
        if (defaultDocumentDirectory.isEmpty()) {
            // add this as a file rather than as a directory?
            args.add(build.getWorkspace().getRemote());
        } else {
            args.add(getResolvedString(defaultDocumentDirectory, build, listener));
        }

        args.add("-GlobalResultsFolder");
        if (globalResultsFolder.isEmpty()) {
            args.add(build.getWorkspace().getRemote());
        } else {
            args.add(getResolvedString(globalResultsFolder, build, listener));
        }
    }

    /**
     * Descriptor for {@link eggPlantBuilder}. Used as a singleton. The class is
     * marked as public so that it can be accessed from views.
     */
    @Override
    public eggPlantBuilder.DescriptorImpl getDescriptor() {
        return (eggPlantBuilder.DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(eggPlantBuilder.class);
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
    {
        @CopyOnWrite
        private volatile EggPlantInstallation[] installations = new EggPlantInstallation[0];

        /**
         * To persist global configuration information, simply store it in a
         * field and call save().
         *
         * <p> If you don't want fields to be persisted, use <tt>transient</tt>.
         */


        public DescriptorImpl() {
            super(eggPlantBuilder.class);
            load();
        }

        public EggPlantInstallation[] getInstallations() {
            return installations;
        }

        public void setInstallations(EggPlantInstallation[] installations) {
            this.installations = installations;
            save();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        @Override
        public String getDisplayName() {
            return "EggPlant";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            save();
            return super.configure(req, formData);
        }
    }
}
