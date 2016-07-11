package org.jenkinsci.tracey;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.TaskListener;
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
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.praqma.tracey.broker.TraceyIOError;
import net.praqma.tracey.broker.TraceyValidatorError;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQBrokerImpl;
import org.jenkinsci.tracey.TraceyHost.TraceyHostDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class TraceyTrigger extends Trigger<Job<?,?>> {

    private static final Logger LOG = Logger.getLogger(TraceyTrigger.class.getName());
    private String exchange = "tracey";
    private TraceyRabbitMQBrokerImpl.ExchangeType type = TraceyRabbitMQBrokerImpl.ExchangeType.TOPIC;
    private String username = "guest";
    private String password = "guest";
    private String consumerTag;
    private transient TraceyRabbitMQBrokerImpl broker;
    private boolean injectEnvironment = false;
    private String envKey = "TRACEY_PAYLOAD";
    private String traceyHost;


    /**
     * Called when the Project is saved.
     * @param project
     * @param newInstance
     */
    @Override
    public void start(final Job<?,?> project, boolean newInstance) {
        super.start(project, newInstance);
        TraceyHost th = TraceyGlobalConfig.getById(traceyHost);
        UsernamePasswordCredentials upw = null;

        if(th != null) {
            StandardCredentials credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(StandardCredentials.class, project, ACL.SYSTEM,
                    Collections.<DomainRequirement>emptyList()), CredentialsMatchers.withId(th.getCredentialId()));
            upw = (UsernamePasswordCredentials)credentials;
        }

        final Jenkins jenkins = Jenkins.getActiveInstance();
        EnvVars env = new EnvVars();

        try {
            env = project.getEnvironment(jenkins, TaskListener.NULL);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "IOException caught", ex);
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "InterruptedException caught", ex);
        }

        if(upw != null) {
            broker = new TraceyRabbitMQBrokerImpl(env.expand(th.getHost()),
                    Secret.toString(upw.getPassword()), upw.getUsername(), type, env.expand(exchange));
        } else {
            broker = new TraceyRabbitMQBrokerImpl(TraceyHostDescriptor.DEFAULT_HOST,
                    env.expand(password), env.expand(username), type, env.expand(exchange));
        }

        broker.getReceiver().setHandler(new TraceyBuildStarter(project, envKey, injectEnvironment));

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
    public TraceyTrigger(String exchange, String traceyHost) {
        this.exchange = exchange;
        this.traceyHost = traceyHost;
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

    /**
     * @return the consumerTag
     */
    public String getConsumerTag() {
        return consumerTag;
    }

    /**
     * @param consumerTag the consumerTag to set
     */
    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    /**
     * @return the traceyHost
     */
    public String getTraceyHost() {
        return traceyHost;
    }

    /**
     * @param traceyHost the traceyHost to set
     */
    public void setTraceyHost(String traceyHost) {
        this.traceyHost = traceyHost;
    }

    /**
     * @return the injectEnvironment
     */
    public boolean isInjectEnvironment() {
        return injectEnvironment;
    }

    /**
     * @param injectEnvironment the injectEnvironment to set
     */
    @DataBoundSetter
    public void setInjectEnvironment(boolean injectEnvironment) {
        this.injectEnvironment = injectEnvironment;
    }

    /**
     * @return the envKey
     */
    public String getEnvKey() {
        return envKey;
    }

    /**
     * @param envKey the envKey to set
     */
    @DataBoundSetter
    public void setEnvKey(String envKey) {
        this.envKey = envKey;
    }

    @Extension
    public static class TraceyTriggerDescriptor extends TriggerDescriptor {

        public static final String DEFAULT_EXCHANGE = "tracey";
        public static final String DEFAULT_ENV_NAME = "TRACEY_PAYLOAD";

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Tracey Trigger";
        }

        public ListBoxModel doFillTraceyHostItems() {
            ListBoxModel model = new ListBoxModel();
            List<TraceyHost> hosts = GlobalConfiguration.all().get(TraceyGlobalConfig.class).getConfiguredHosts();
            if(hosts != null) {
                for(TraceyHost th : hosts) {
                    model.add(th.getDescription(), th.getCredentialId());
                }
            }
            return model;
        }

    }

}
