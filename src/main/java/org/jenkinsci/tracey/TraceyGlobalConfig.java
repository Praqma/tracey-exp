package org.jenkinsci.tracey;

import hudson.Extension;
import java.util.List;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class TraceyGlobalConfig extends GlobalConfiguration {

    private List<TraceyHost> configuredHosts;

    @DataBoundConstructor
    public TraceyGlobalConfig(List<TraceyHost> configuredHosts) {
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
    public List<TraceyHost> getConfiguredHosts() {
        return configuredHosts;
    }

    /**
     * @param configuredHosts the configuredHosts to set
     */
    public void setConfiguredHosts(List<TraceyHost> configuredHosts) {
        this.configuredHosts = configuredHosts;
    }

    public static TraceyGlobalConfig get() {
        return GlobalConfiguration.all().get(TraceyGlobalConfig.class);
    }

    public static TraceyHost getById(String id) {
        for(TraceyHost th : get().configuredHosts) {
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
