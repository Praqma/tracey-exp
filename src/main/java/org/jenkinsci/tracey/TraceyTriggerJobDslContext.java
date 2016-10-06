package org.jenkinsci.tracey;

import groovy.lang.Closure;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.ContextHelper;
import net.praqma.tracey.broker.api.TraceyFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * The context to resolve Job DSL Tracey configure blocks in
 */
public class TraceyTriggerJobDslContext implements Context {
    public List<TraceyFilter> filters = new ArrayList<>();
    public boolean injectEnvironment = false;
    public boolean injectGitVariables = false;
    public String envKey = TraceyTrigger.TraceyTriggerDescriptor.DEFAULT_ENV_NAME;
    public String payloadInjection;

    /**
     * Configures filters for the Tracey Trigger
     * @param closure Closure containing filter configuration
     */
    public void filters(Closure closure) {
        TraceyFilterJobDslContext context = new TraceyFilterJobDslContext();
        ContextHelper.executeInContext(closure, context);
        filters.addAll(context.filters);
    }

    /**
     * Configures environment injection
     * @param injEnv Whether or not to inject environment variables
     */
    public void injectEnvironment(boolean injEnv) {
        injectEnvironment(injEnv, null);
    }

    /**
     * Configures environment injection
     * @param closure Contains environment injection configuration
     */
    public void injectEnvironment(Closure closure) {
        injectEnvironment(true, closure);
    }

    /**
     * Toggles EnvVar injection
     * @param injEnv Whether or not to inject environment variables
     * @param closure Contains environment injection configuration
     */
    public void injectEnvironment(boolean injEnv, Closure closure) {
        injectEnvironment = injEnv;
        if(closure != null) {
            TraceyInjectEnvironmentDslContext context = new TraceyInjectEnvironmentDslContext();
            ContextHelper.executeInContext(closure, context);
            injectGitVariables = context.injectGitVariables;
            envKey = context.envKey;
            payloadInjection = context.payloadInjection;
        }
    }
}
