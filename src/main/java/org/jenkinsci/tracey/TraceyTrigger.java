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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildableItem;
import hudson.model.Cause;
import hudson.model.EnvironmentContributingAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQBrokerImpl;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Mads
 */
public class TraceyTrigger extends Trigger<AbstractProject<?,?>> {

    private static final Logger LOG = Logger.getLogger(TraceyTrigger.class.getName());
    private String exchange = "tracey";

    @Override
    public void start(final AbstractProject<?,?> project, boolean newInstance) {
        try {
            TraceyRabbitMQBrokerImpl p = new TraceyRabbitMQBrokerImpl();
            Channel c = p.setUpChannel(exchange);
            Consumer cu = new DefaultConsumer(c) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, final byte[] body) throws IOException {
                    EnvironmentContributingAction action = new EnvironmentContributingAction() {
                        @Override
                        public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
                            try {
                                env.put("TRACEY_PAYLOAD", new String(body, "UTF-8"));
                            } catch (UnsupportedEncodingException ex) {
                                LOG.log(Level.SEVERE, "Error", ex);
                            }
                        }

                        @Override
                        public String getIconFileName() {
                            return null;
                        }

                        @Override
                        public String getDisplayName() {
                            return null;
                        }

                        @Override
                        public String getUrlName() {
                            return null;
                        }
                    };
                    project.scheduleBuild2(3, new Cause.UserIdCause(), action);
                }
            };

            p.recieve(exchange, cu);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in triggering!", e);
        }
    }

    private <T extends Item> BuildableItem asBuildable(T j) {
        if(j instanceof BuildableItem) {
            return (BuildableItem)j;
        }
        return null;
    }

    @DataBoundConstructor
    public TraceyTrigger(String exchange) {
        this.exchange = exchange;
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

    @Extension
    public static class TraceyTriggerDescriptor extends TriggerDescriptor {

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Tracey Trigger";
        }
    }
}
