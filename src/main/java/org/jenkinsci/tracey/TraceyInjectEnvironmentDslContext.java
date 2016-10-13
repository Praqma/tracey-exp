package org.jenkinsci.tracey;

import javaposse.jobdsl.dsl.Context;
import org.apache.commons.lang.StringUtils;

public class TraceyInjectEnvironmentDslContext implements Context {
    public String envKey = RabbitMQTrigger.RabbitMQTriggerDescriptor.DEFAULT_ENV_NAME;
    public String payloadInjection;

    /**
     * Sets EnvVar key for message Payload
     * @param key EnvVar key to assign the payload to
     */
    public void payloadKey(String key) {
        envKey = key;
    }

    /**
     * Adds payload injection lines
     * @param lines Payload EnvVar injection entries
     */
    public void payloadInjection(String... lines) {
        payloadInjection = StringUtils.join(lines, "\n");
    }
}
