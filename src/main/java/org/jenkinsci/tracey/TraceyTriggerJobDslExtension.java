package org.jenkinsci.tracey;

import groovy.lang.Closure;
import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.triggers.TriggerContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

/**
 * Extension point for Jenkins Job DSL support
 */
@Extension(optional = true)
public class TraceyTriggerJobDslExtension extends ContextExtensionPoint {

    /**
     * Configures a Tracey trigger for a job
     * @param exchange The exchange to listen to
     * @param host The Tracey host name
     * @return A configured Tracey trigger
     */
    @RequiresPlugin(id = "tracey", minimumVersion = "1.0-SNAPSHOT")
    @DslExtensionMethod(context = TriggerContext.class)
    public Object tracey(String exchange, String host) {
        return tracey(exchange, host, null);
    }

    /**
     * Configures a Tracey trigger for a job
     * @param exchange The exchange to listen to
     * @param host The Tracey host name
     * @param closure Closure containing Tracey trigger configuration
     * @return A configured Tracey trigger
     */
    @RequiresPlugin(id = "tracey", minimumVersion = "1.0-SNAPSHOT")
    @DslExtensionMethod(context = TriggerContext.class)
    public Object tracey(String exchange, String host, Closure closure) {
        TraceyTriggerJobDslContext context = new TraceyTriggerJobDslContext();
        if(closure!= null) executeInContext(closure, context);
        TraceyTrigger trigger = new TraceyTrigger(exchange, host,
                context.injectEnvironment,
                context.injectGitVariables,
                context.envKey,
                context.filters);
        trigger.setRegexToEnv(context.payloadInjection);
        return trigger;
    }
}
