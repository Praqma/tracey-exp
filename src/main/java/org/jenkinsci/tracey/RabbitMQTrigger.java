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
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.praqma.tracey.broker.api.TraceyFilter;
import net.praqma.tracey.broker.api.TraceyIOError;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQConnection;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQDefaults;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQRoutingInfo;
import net.praqma.tracey.broker.impl.rabbitmq.TraceyRabbitMQBrokerImpl;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.tracey.filter.PayloadJSONBasicFilter;
import org.jenkinsci.tracey.filter.PayloadJSONRegexFilter;
import org.jenkinsci.tracey.filter.TraceyPayloadRegexFilter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RabbitMQTrigger extends Trigger<Job<?,?>> {

    private static final Logger LOG = Logger.getLogger(RabbitMQTrigger.class.getName());
    private String exchangeName = RabbitMQDefaults.EXCHANGE_NAME;
    private String exchangeType = RabbitMQDefaults.EXCHANGE_TYPE;
    private String consumerTag;
    private transient TraceyRabbitMQBrokerImpl broker;
    private transient RabbitMQRoutingInfo info;

    private String envKey = "TRACEY_PAYLOAD";
    private String rabbitMQHost;

    //TODO: This should be part of a build wrapper
    private boolean injectEnvironment = false;

    //Post receive filters
    private List<TraceyFilter> filters = new ArrayList<>();
    private String regexToEnv;

    /**
     * Called when the Project is saved.
     * @param project The current project/job to which this trigger belongs
     * @param newInstance is this trigger just being added? true/false
     */
    @Override
    public void start(final Job<?,?> project, boolean newInstance) {
        super.start(project, newInstance);

        broker = configureBroker(project, rabbitMQHost);
        info = new RabbitMQRoutingInfo();
        info.setExchangeName(exchangeName);
        info.setExchangeType(exchangeType);

        TraceyBuildStarter tbs = new TraceyBuildStarter(project, envKey, filters);
        LOG.info(tbs.toString());
        broker.getReceiver().setHandler(tbs);

        try {
            consumerTag = broker.receive(info);
        } catch (TraceyIOError ex) {
            LOG.log(Level.SEVERE, "IOError caught", ex);
        }

    }

    private UsernamePasswordCredentials getCredentials(RabbitMQHost h, Job<?,?> j) {
        UsernamePasswordCredentials upw = null;
        if(h != null) {
            StandardCredentials credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(StandardCredentials.class, j, ACL.SYSTEM,
                    Collections.<DomainRequirement>emptyList()), CredentialsMatchers.withId(h.getCredentialId()));
            upw = (UsernamePasswordCredentials)credentials;
        }
        return upw;
    }

    private TraceyRabbitMQBrokerImpl configureBroker(Job<?,?> proj, String hid) {
        RabbitMQHost rh = TraceyGlobalConfig.getById(hid);
        UsernamePasswordCredentials upw = getCredentials(rh, proj);
        String rHost = rh.getHost();

        final Jenkins jenkins = Jenkins.getInstance();
        EnvVars env = new EnvVars();

        try {
            env = proj.getEnvironment(jenkins, TaskListener.NULL);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "IOException caught", ex);
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "InterruptedException caught", ex);
        }
        if(upw != null) {
            broker = new TraceyRabbitMQBrokerImpl(new RabbitMQConnection(env.expand(rHost),
                    rh.getRabbitMQPort(),
                    upw.getUsername(),
                    Secret.toString(upw.getPassword()),
                    RabbitMQDefaults.AUTOMATIC_RECOVERY),
                    filters);
        } else {
            broker = new TraceyRabbitMQBrokerImpl(new RabbitMQConnection(RabbitMQDefaults.HOST,
                                                RabbitMQDefaults.PORT,
                                                env.expand(RabbitMQDefaults.USERNAME),
                                                env.expand(RabbitMQDefaults.PASSWORD),
                                                RabbitMQDefaults.AUTOMATIC_RECOVERY),
                                                filters);
        }
        return broker;
    }

    @Override
    public void stop() {
        try {
            super.stop();
            if(broker != null) {
                broker.getReceiver().cancel(consumerTag);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to stop consumer", ex);
        }
    }

    @DataBoundConstructor
    public RabbitMQTrigger(String exchangeName, String rabbitMQHost, boolean injectEnvironment, String envKey, List<TraceyFilter> filters) {
        this.exchangeName = exchangeName;
        this.rabbitMQHost = rabbitMQHost;
        this.injectEnvironment = injectEnvironment;
        this.envKey = envKey;
        this.filters = filters;
    }

    /**
     * @return the exchangeName
     */
    public String getExchangeName() {
        return exchangeName;
    }

    /**
     * @param exchangeName the exchangeName to set
     */
    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    @Override
    public RabbitMQTriggerDescriptor getDescriptor() {
        return (RabbitMQTriggerDescriptor)super.getDescriptor();
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
     * @return the rabbitMQHost
     */
    public String getRabbitMQHost() {
        return rabbitMQHost;
    }

    /**
     * @param rabbitMQHost the rabbitMQHost to set
     */
    public void setRabbitMQHost(String rabbitMQHost) {
        this.rabbitMQHost = rabbitMQHost;
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

    public String getExchangeType() {
        return exchangeType;
    }
    @DataBoundSetter
    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    @Extension
    public static class RabbitMQTriggerDescriptor extends TriggerDescriptor {

        public static final String DEFAULT_ENV_NAME = "TRACEY_PAYLOAD";

        public RabbitMQTriggerDescriptor() {
            load();
        }

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "RabbitMQ Trigger";
        }

        @Override
        public Trigger<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return super.newInstance(req, formData); //To change body of generated methods, choose Tools | Templates.
        }

        public ListBoxModel doFillRabbitMQHostItems() {
            ListBoxModel model = new ListBoxModel();
            TraceyGlobalConfig conf = GlobalConfiguration.all().get(TraceyGlobalConfig.class);
            if(conf != null) {
                List<RabbitMQHost> hosts = conf.getConfiguredHosts();
                if(hosts != null) {
                    for(RabbitMQHost rh : hosts) {
                        model.add(rh.getDescription(), rh.getHostId());
                    }
                }
            }
            return model;
        }

        public ListBoxModel doFillExchangeTypeItems() {
            ListBoxModel model = new ListBoxModel();
            model.add("topic", "topic");
            model.add("direct", "direct");
            model.add("fanout", "fanout");
            model.add("headers", "headers");
            return model;
        }

        public static List<Descriptor> getFilters() {
            List<Descriptor> descriptorz = new ArrayList<>();
            descriptorz.add(Jenkins.getInstance().getDescriptorByType(TraceyPayloadRegexFilter.PayloadRegexFilterDescriptor.class));
            descriptorz.add(Jenkins.getInstance().getDescriptorByType(PayloadJSONBasicFilter.PayloadJSONBasicFilterDescriptor.class));
            descriptorz.add(Jenkins.getInstance().getDescriptorByType(PayloadJSONRegexFilter.PayloadJSONRegexFilterDescriptor.class));
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
