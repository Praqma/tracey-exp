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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQBrokerImpl;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Mads
 */
public class TraceyTrigger extends Trigger<Job<?,?>> {

    private static final Logger LOG = Logger.getLogger(TraceyTrigger.class.getName());
    private String exchange = "tracey";
    private String host = "localhost";
    private String credentialId;
    private TraceyRabbitMQBrokerImpl.ExchangeType type = TraceyRabbitMQBrokerImpl.ExchangeType.FANOUT;

    private transient ExecutorService es = Executors.newFixedThreadPool(1);
    private transient Future<TraceyAsyncListener> listener;


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

    //public TraceyRabbitMQBrokerImpl(String host, String password, String user, ExchangeType type, String exchange) {
    class TraceyAsyncListener implements Runnable {

        Job<?,?> project;
        String exchange = "tracey";
        String username = "guest";
        String password = "guest";
        String host = "locahost";

        public TraceyAsyncListener(final String exchange, final Job<?,?> project, String username, String password, String host) {
            this.project = project;
            this.exchange = exchange;
            if(username != null && !username.trim().isEmpty())
                this.username = username;
            if(password != null && !password.trim().isEmpty())
                this.password = password;
            this.host = host;
        }

        @Override
        public void run() {
            try {
                TraceyRabbitMQBrokerImpl p = new TraceyRabbitMQBrokerImpl(host, password, username, type, exchange);
                p.getReceiver().setHandler(new TraceyBuildStarter(project));
                p.receive(exchange);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in triggering!", e);
            }
        }
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

        if(newInstance) {
            if(es != null) {
                if(upw != null) {
                    listener = (Future<TraceyAsyncListener>)es.submit(new TraceyAsyncListener(exchange, project, upw.getUsername(), Secret.toString(upw.getPassword()), host));
                } else {
                    listener = (Future<TraceyAsyncListener>)es.submit(new TraceyAsyncListener(exchange, project, null, null, host));
                }
            }
        }

    }

    /**
     * Called when the project is recofigured as well. So when saved stop() -> start()
     */
    @Override
    public void stop() {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
        listener.cancel(true);
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
