package eggPlant;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import java.io.IOException;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author gcampb2
 */
public final class EggPlantInstallation
    extends ToolInstallation
    implements NodeSpecific<EggPlantInstallation>, EnvironmentSpecific<EggPlantInstallation>
{
    @DataBoundConstructor
    public EggPlantInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(Util.fixEmptyAndTrim(name), Util.fixEmptyAndTrim(home), properties);
    }

    @Override
    public EggPlantInstallation forNode(Node node, TaskListener tl) throws IOException, InterruptedException {
        return new EggPlantInstallation(getName(), translateFor(node, tl), getProperties().toList());
    }

    @Override
    public EggPlantInstallation forEnvironment(EnvVars env) {
        return new EggPlantInstallation(getName(), env.expand(getHome()), getProperties().toList());
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<EggPlantInstallation> {

        public DescriptorImpl() {
        }

        @Override
        public String getDisplayName() {
            return "EggPlant";
        }

        @Override
        public EggPlantInstallation[] getInstallations() {
            return Hudson.getInstance().getDescriptorByType(eggPlantBuilder.DescriptorImpl.class).getInstallations();
        }

        @Override
        public void setInstallations(EggPlantInstallation... installations) {
            Hudson.getInstance().getDescriptorByType(eggPlantBuilder.DescriptorImpl.class).setInstallations(installations);
            save();
        }
   }
}
