/*
 * The MIT License
 *
 * Copyright 2016 Mads.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.tracey;

import hudson.Extension;
import java.util.List;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Mads
 */
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
            if(th.getCredentialId().equals(id)) {
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
