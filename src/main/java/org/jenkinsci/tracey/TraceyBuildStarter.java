package org.jenkinsci.tracey;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Job;
import java.io.IOException;
import jenkins.model.ParameterizedJobMixIn;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQMessageHandler;

/**
 *
 * @author Mads
 */
public class TraceyBuildStarter implements TraceyRabbitMQMessageHandler {

    private Job<?,?> project;

    public TraceyBuildStarter(final Job<?,?> project) {
        this.project = project;
    }

    @Override
    public void handleDelivery(String string, Envelope envlp, AMQP.BasicProperties bp, byte[] bytes) throws IOException {
        ParameterizedJobMixIn jobMix =  new ParameterizedJobMixIn() {
            @Override
            protected Job asJob() {
                return project;
            }
        };
        jobMix.scheduleBuild2(3, new CauseAction(new Cause.UserIdCause()), new TraceyAction(new String(bytes, "UTF-8")));
    }
}
