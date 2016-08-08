package org.jenkinsci.tracey;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.Collections;
import java.util.List;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class TraceyHost implements Describable<TraceyHost> {

    private String host;
    private String credentialId;
    private String description;
    private int traceyPort = 5672;

    @DataBoundConstructor
    public TraceyHost(String host, String credentialId, String description, int traceyPort) {
        this.credentialId = credentialId;
        this.host = host;
        this.description = description;
        this.traceyPort = traceyPort;
    }

    @Override
    public Descriptor<TraceyHost> getDescriptor() {
        return new TraceyHostDescriptor();
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the credentialId
     */
    public String getCredentialId() {
        return credentialId;
    }

    /**
     * @param credentialId the credentialId to set. This is set using form binding
     *        with the provided credentials control. 
     */
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the traceyPort
     */
    public int getTraceyPort() {
        return traceyPort;
    }

    /**
     * @param traceyPort the traceyPort to set
     */
    public void setTraceyPort(int traceyPort) {
        this.traceyPort = traceyPort;
    }


    @Extension
    public static class TraceyHostDescriptor extends Descriptor<TraceyHost> {

        public static final String DEFAULT_HOST = "localhost";
        public static final int DEFAULT_PORT = 5672;

        @Override
        public String getDisplayName() {
            return "Tracey Host";
        }


        public ListBoxModel doFillCredentialIdItems(final @AncestorInPath ItemGroup<?> context) {
            final List<StandardCredentials> credentials = CredentialsProvider.lookupCredentials(StandardCredentials.class, context, ACL.SYSTEM, Collections.<DomainRequirement>emptyList());

            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withMatching(CredentialsMatchers.anyOf(
                            CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class)
                    ), credentials);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            save();
            return true;
        }

        public FormValidation doCheckDescription(@QueryParameter String description) {
            if (StringUtils.isBlank(description)) {
                return FormValidation.error("Description must be specified");
            }
            return FormValidation.ok();
        }

    }

}
