package org.jenkinsci.tracey;

import javaposse.jobdsl.dsl.Context;
import org.apache.commons.lang.StringUtils;

public class TraceyInjectEnvironmentDslContext implements Context {
    public String envKey = TraceyTrigger.TraceyTriggerDescriptor.DEFAULT_ENV_NAME;
    public boolean injectGitVariables = false;
    public String payloadInjection;

    /**
     * Sets EnvVar key for message Payload
     * @param key EnvVar key to assign the payload to
     */
    public void payloadKey(String key) {
        envKey = key;
    }

    /**
     * Toggles Git variable injection
     * @param injGit Whether or not to inject Git environment variables
     */
    public void injectGitVariables(boolean injGit) {
        injectGitVariables = injGit;
    }

    /**
     * Adds payload injection lines
     * @param lines Payload EnvVar injection entries
     */
    public void payloadInjection(String... lines) {
        payloadInjection = StringUtils.join(lines, "\n");
    }
}
