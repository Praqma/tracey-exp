package org.jenkinsci.tracey;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Job;
import java.io.IOException;
import java.util.logging.Logger;
import jenkins.model.ParameterizedJobMixIn;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQMessageHandler;

/**
 *
 * @author Mads
 */
public class TraceyBuildStarter implements TraceyRabbitMQMessageHandler {

    private static final Logger LOG = Logger.getLogger(TraceyBuildStarter.class.getName());

    private Job<?,?> project;
    private String envKey;

    public TraceyBuildStarter(final Job<?,?> project, String envKey) {
        this.project = project;
        this.envKey = envKey;
    }

    @Override
    public void handleDelivery(String string, Envelope envlp, AMQP.BasicProperties bp, byte[] bytes) throws IOException {
        ParameterizedJobMixIn jobMix =  new ParameterizedJobMixIn() {
            @Override
            protected Job asJob() {
                return project;
            }
        };
        TraceyAction tAction = new TraceyAction(new String(bytes, "UTF-8"), envKey);
        LOG.info("Tracey Action added for job "+project.getName());
        LOG.info(tAction.toString());
        jobMix.scheduleBuild2(3, new CauseAction(new Cause.UserIdCause()), tAction);
    }

    @Override
    public String toString() {
        return String.format("TraceyBuildStarter[envKey = %s, project = %s]",
                envKey,
                project.getName());
    }


}
