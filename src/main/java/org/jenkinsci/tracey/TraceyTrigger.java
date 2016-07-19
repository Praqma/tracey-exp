package org.jenkinsci.tracey;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.praqma.tracey.broker.TraceyIOError;
import net.praqma.tracey.broker.TraceyValidatorError;
import net.praqma.tracey.broker.rabbitmq.TraceyFilter;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQBrokerImpl;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.tracey.TraceyHost.TraceyHostDescriptor;
import org.jenkinsci.tracey.filter.EiffelEventTypeFilter.EiffelEventTypeFilterDescriptor;
import org.jenkinsci.tracey.filter.EiffelPayloadRegexFilter.EiffelPayloadRegexFilterDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class TraceyTrigger extends Trigger<Job<?,?>> {

    private static final Logger LOG = Logger.getLogger(TraceyTrigger.class.getName());
    private String exchange = "tracey";
    private TraceyRabbitMQBrokerImpl.ExchangeType type = TraceyRabbitMQBrokerImpl.ExchangeType.TOPIC;
    private String consumerTag;
    private transient TraceyRabbitMQBrokerImpl broker;

    private String envKey = "TRACEY_PAYLOAD";
    private String traceyHost;

    //TODO: This should be part of a build wrapper
    private boolean injectEnvironment = false;
    private boolean gitReady;

    //Post recieve fileters
    private List<TraceyFilter> filters = new ArrayList<TraceyFilter>();
    private String regexToEnv;

    /**
     * Called when the Project is saved.
     * @param project
     * @param newInstance
     */
    @Override
    public void start(final Job<?,?> project, boolean newInstance) {
        super.start(project, newInstance);

        broker = configureBroker(project, traceyHost, type, exchange);

        TraceyBuildStarter tbs = new TraceyBuildStarter(project, envKey, filters);
        LOG.info(tbs.toString());
        if(filters != null) {
            LOG.info(String.format("Filters for job %s", project.getName()));
            for(TraceyFilter ft : filters) {
                LOG.info(ft.getClass().getSimpleName());
            }
            broker.getReceiver().getFilters().addAll(filters);
        }
        broker.getReceiver().setHandler(tbs);

        try {
            consumerTag = broker.receive(getExchange());
        } catch (TraceyValidatorError ex) {
            LOG.log(Level.INFO, "Failed to validate", ex);
        } catch (TraceyIOError ex) {
            LOG.log(Level.SEVERE, "IOError caught", ex);
        }

    }

    private UsernamePasswordCredentials getCredentials(TraceyHost h, Job<?,?> j) {
        UsernamePasswordCredentials upw = null;
        if(h != null) {
            StandardCredentials credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(StandardCredentials.class, j, ACL.SYSTEM,
                    Collections.<DomainRequirement>emptyList()), CredentialsMatchers.withId(h.getCredentialId()));
            upw = (UsernamePasswordCredentials)credentials;
        }
        return upw;
    }

    private TraceyRabbitMQBrokerImpl configureBroker(Job<?,?> proj, String hid, TraceyRabbitMQBrokerImpl.ExchangeType t, String exch) {
        TraceyHost th = TraceyGlobalConfig.getById(hid);
        UsernamePasswordCredentials upw = getCredentials(th, proj);
        String tHost = th.getHost();

        final Jenkins jenkins = Jenkins.getActiveInstance();
        EnvVars env = new EnvVars();

        try {
            env = proj.getEnvironment(jenkins, TaskListener.NULL);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "IOException caught", ex);
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "InterruptedException caught", ex);
        }

        if(upw != null) {
            broker = new TraceyRabbitMQBrokerImpl(env.expand(tHost),
                    Secret.toString(upw.getPassword()), upw.getUsername(), t, env.expand(exch), th.getTraceyPort());
        } else {
            broker = new TraceyRabbitMQBrokerImpl(TraceyHostDescriptor.DEFAULT_HOST,
                    env.expand(TraceyTriggerDescriptor.DEFAULT_PASSWORD), env.expand(TraceyTriggerDescriptor.DEFAULT_USER),
                    t, env.expand(exch));
        }
        return broker;
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
    public TraceyTrigger(String exchange, String traceyHost, boolean injectEnvironment, boolean gitReady, String envKey, List<TraceyFilter> filters) {
        this.exchange = exchange;
        this.traceyHost = traceyHost;
        this.injectEnvironment = injectEnvironment;
        this.gitReady = gitReady;
        this.envKey = envKey;
        this.filters = filters;
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

    /**
     * @return the gitReady
     */
    public boolean isGitReady() {
        return gitReady;
    }

    /**
     * @param gitReady the gitReady to set
     */
    public void setGitReady(boolean gitReady) {
        this.gitReady = gitReady;
    }

    /**
     * @return the filters
     */
    public List<TraceyFilter> getFilters() {
        return filters;
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(List<TraceyFilter> filters) {
        this.filters = filters;
    }

    /**
     * @return the regexToEnv
     */
    public String getRegexToEnv() {
        return regexToEnv;
    }

    /**
     * @param regexToEnv the regexToEnv to set
     */
    @DataBoundSetter
    public void setRegexToEnv(String regexToEnv) {
        this.regexToEnv = regexToEnv;
    }

    @Extension
    public static class TraceyTriggerDescriptor extends TriggerDescriptor {

        public static final String DEFAULT_EXCHANGE = "tracey";
        public static final String DEFAULT_USER = "guest";
        public static final String DEFAULT_PASSWORD = "guest";
        public static final String DEFAULT_ENV_NAME = "TRACEY_PAYLOAD";

        public TraceyTriggerDescriptor() {
            load();
        }

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Tracey Trigger";
        }


        @Override
        public Trigger<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return super.newInstance(req, formData); //To change body of generated methods, choose Tools | Templates.
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

        public static List<Descriptor> getFilters() {
            List<Descriptor> descriptorz = new ArrayList<Descriptor>();
            descriptorz.add(Jenkins.getActiveInstance().getDescriptorByType(EiffelPayloadRegexFilterDescriptor.class));
            descriptorz.add(Jenkins.getActiveInstance().getDescriptorByType(EiffelEventTypeFilterDescriptor.class));
            return descriptorz;
        }

        public FormValidation doCheckRegexToEnv(@QueryParameter String regexToEnv) {
            if(!StringUtils.isBlank(regexToEnv)) {
                String[] lines = regexToEnv.split("[\\r\\n]+");
                for(String line : lines) {
                    String[] comp = line.split("\\s");
                    if(comp.length == 2) {
                        String key = comp[0];
                        String regex = comp[1];
                    }
                }
            }
            return FormValidation.ok();
        }

    }

}
