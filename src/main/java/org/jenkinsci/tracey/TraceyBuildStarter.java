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
