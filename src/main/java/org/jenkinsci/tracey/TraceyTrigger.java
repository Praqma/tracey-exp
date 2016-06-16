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

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private transient ExecutorService es = Executors.newFixedThreadPool(1);

    class TraceyAsyncListener implements Runnable {

        AbstractProject<?,?> project;
        String exchange;

        public TraceyAsyncListener(final String exchange, final AbstractProject<?,?> project ) {
            this.project = project;
            this.exchange = exchange;
        }

        @Override
        public void run() {
            try {
                TraceyRabbitMQBrokerImpl p = new TraceyRabbitMQBrokerImpl();
                p.getReceiver().setHandler(new TraceyBuildStarter(project));
                p.receive(exchange);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in triggering!", e);
            }
        }
    }

    @Override
    public void start(final AbstractProject<?,?> project, boolean newInstance) {
        if(newInstance) {
            es.submit(new TraceyAsyncListener(exchange, project));
        }
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

    @Override
    public TraceyTriggerDescriptor getDescriptor() {
        return (TraceyTriggerDescriptor)super.getDescriptor();
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
