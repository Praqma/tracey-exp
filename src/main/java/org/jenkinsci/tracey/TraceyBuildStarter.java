package org.jenkinsci.tracey;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Job;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import net.praqma.tracey.broker.api.TraceyFilter;
import net.praqma.tracey.broker.impl.rabbitmq.TraceyRabbitMQMessageHandler;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Mads
 */
public class TraceyBuildStarter implements TraceyRabbitMQMessageHandler {

    private static final Logger LOG = Logger.getLogger(TraceyBuildStarter.class.getName());

    private Job<?,?> project;
    private String envKey;
    private List<TraceyFilter> filter;

    public TraceyBuildStarter(final Job<?,?> project, String envKey, List<TraceyFilter> filter) {
        this.project = project;
        this.envKey = envKey;
        this.filter = filter;
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        LOG.info(String.format("Tracey called handleCancel for %s", consumerTag));
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOG.info(String.format("Tracey called handleShutdownSignal for %s", consumerTag));
    }

    @Override
    public void handleDelivery(String s, Envelope envlp, AMQP.BasicProperties bp, byte[] bytes) throws IOException {
        ParameterizedJobMixIn jobMix =  new ParameterizedJobMixIn() {
            @Override
            protected Job asJob() {
                return project;
            }
        };

        boolean shouldSpawnJob = true;

        if(filter != null) {
            for(TraceyFilter f : filter) {
                boolean response = f.postReceive(new String(bytes, "UTF-8")) != null;
                if(!response) {
                    LOG.info(String.format("Filter %s rejected payload: %s",f.getClass().getSimpleName(), new String(bytes, "UTF-8")));
                }
                shouldSpawnJob &= response;
            }
        } else {
            LOG.info(String.format("No filters for job %s", project.getName()));
        }

        if(shouldSpawnJob) {
            TraceyAction tAction = new TraceyAction(new String(bytes, "UTF-8"), envKey);
            jobMix.scheduleBuild2(Jenkins.getInstance().getQuietPeriod(), new CauseAction(new Cause.UserIdCause()), tAction);
        }
    }

    @Override
    public String toString() {
        return String.format("TraceyBuildStarter[envKey = %s, project = %s]",
                envKey,
                project.getName());
    }


}
