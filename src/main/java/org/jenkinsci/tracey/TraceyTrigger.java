package org.jenkinsci.tracey;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.praqma.tracey.broker.TraceyIOError;
import net.praqma.tracey.broker.TraceyValidatorError;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQBrokerImpl;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

public class TraceyTrigger extends Trigger<Job<?,?>> {

    private static final Logger LOG = Logger.getLogger(TraceyTrigger.class.getName());
    private String exchange = "tracey";
    private String host = "localhost";
    private String credentialId;
    private TraceyRabbitMQBrokerImpl.ExchangeType type = TraceyRabbitMQBrokerImpl.ExchangeType.FANOUT;
    private String username = "guest";
    private String password = "guest";
    private String consumerTag;
    private transient TraceyRabbitMQBrokerImpl broker;

    /**
     * @return the credentialId
     */
    public String getCredentialId() {
        return credentialId;
    }

    /**
     * @param credentialId the credentialId to set
     */
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
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
     * Called when the Project is saved.
     * @param project
     * @param newInstance
     */
    @Override
    public void start(final Job<?,?> project, boolean newInstance) {
        super.start(project, newInstance);
        StandardCredentials credentials = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(StandardCredentials.class, project, ACL.SYSTEM,
                Collections.<DomainRequirement>emptyList()), CredentialsMatchers.withId(credentialId));
        UsernamePasswordCredentials upw = (UsernamePasswordCredentials)credentials;

        if(upw != null) {
            broker = new TraceyRabbitMQBrokerImpl(host, Secret.toString(upw.getPassword()), upw.getUsername(), type, exchange);
        } else {
            broker = new TraceyRabbitMQBrokerImpl(host, password, username, type, exchange);
        }

        broker.getReceiver().setHandler(new TraceyBuildStarter(project));
        
        try {
            consumerTag = broker.receive(getExchange());
        } catch (TraceyValidatorError ex) {
            LOG.log(Level.INFO, "Failed to validate", ex);
        } catch (TraceyIOError ex) {
            LOG.log(Level.SEVERE, "IOError caught", ex);
        }

    }

    /**
     * Called when the project is reconfigured as well. So when saved stop() -> start()
     */
    @Override
    public void stop() {
        try {
            super.stop();
            //Cancel the consumer that is on the old consumer. We create a new one
            //for every job saved.
            broker.getReceiver().cancel(consumerTag);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to stop consumer", ex);
        }
    }



    @DataBoundConstructor
    public TraceyTrigger(String exchange, String credentialId, String host) {
        this.exchange = exchange;
        this.credentialId = credentialId;
        this.host = host;
    }

    /**
     * @return the exchange
     */
    public String getExchange() {
        return exchange;
    }

    /**
     * @param exchange the exchange to set
     */
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @Override
    public TraceyTriggerDescriptor getDescriptor() {
        return (TraceyTriggerDescriptor)super.getDescriptor();
    }

    @Extension
    public static class TraceyTriggerDescriptor extends TriggerDescriptor {

        public static final String DEFAULT_HOST = "localhost";
        public static final String DEFAULT_EXCHANGE = "tracey";

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Tracey Trigger";
        }

        public ListBoxModel doFillCredentialIdItems(final @AncestorInPath ItemGroup<?> context) {
            final List<StandardCredentials> credentials = CredentialsProvider.lookupCredentials(StandardCredentials.class, context, ACL.SYSTEM, Collections.<DomainRequirement>emptyList());

            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withMatching(CredentialsMatchers.anyOf(
                            CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class)
                    ), credentials);
        }

    }

}
