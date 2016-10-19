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
     * @param exchangeName The exchangeName to listen to
     * @param hostId The Tracey host name
     * @param exchangeType Exchange type
     * @return A configured Tracey trigger
     */
    @RequiresPlugin(id = "tracey", minimumVersion = "1.0-SNAPSHOT")
    @DslExtensionMethod(context = TriggerContext.class)
    public Object tracey(String exchangeName, String hostId, String exchangeType) {
        return tracey(exchangeName, hostId, exchangeType, null);
    }

    /**
     * Configures a Tracey trigger for a job
     * @param exchangeName The exchange to listen to
     * @param exchangeType Exchange type
     * @param hostId The Tracey host name
     * @param closure Closure containing Tracey trigger configuration
     * @return A configured Tracey trigger
     */
    @RequiresPlugin(id = "tracey", minimumVersion = "1.0-SNAPSHOT")
    @DslExtensionMethod(context = TriggerContext.class)
    public Object tracey(String exchangeName, String exchangeType, String hostId, Closure closure) {
        TraceyTriggerJobDslContext context = new TraceyTriggerJobDslContext();
        if(closure!= null) executeInContext(closure, context);
        RabbitMQTrigger trigger = new RabbitMQTrigger(exchangeName, hostId,
                context.injectEnvironment,
                context.envKey,
                context.filters);
        trigger.setRegexToEnv(context.payloadInjection);
        trigger.setExchangeType(exchangeType);
        return trigger;
    }
}
