package org.jenkinsci.tracey;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;

@Extension
public class TraceyGlobalConfig extends GlobalConfiguration {

    private List<RabbitMQHost> configuredHosts;

    @DataBoundConstructor
    public TraceyGlobalConfig(List<RabbitMQHost> configuredHosts) {
        this.configuredHosts = configuredHosts;
    }

    public TraceyGlobalConfig() { load(); }

    @Override
    public String getDisplayName() {
        return "Tracey host configuration";
    }

    /**
     * @return the configuredHosts
     */
    public List<RabbitMQHost> getConfiguredHosts() {
        return configuredHosts;
    }

    /**
     * @param configuredHosts the configuredHosts to set
     */
    public void setConfiguredHosts(List<RabbitMQHost> configuredHosts) {
        this.configuredHosts = configuredHosts;
    }

    public static TraceyGlobalConfig get() {
        return GlobalConfiguration.all().get(TraceyGlobalConfig.class);
    }

    public static RabbitMQHost getById(String id) {
        for(RabbitMQHost th : get().configuredHosts) {
            if(th.getHostId().equals(id)) {
                return th;
            }
        }
        return null;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return super.configure(req, json);
    }

}
