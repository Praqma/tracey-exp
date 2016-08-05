package org.jenkinsci.tracey;

import groovy.lang.Closure;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.ContextHelper;
import net.praqma.tracey.broker.rabbitmq.TraceyFilter;
import org.jenkinsci.tracey.filter.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Context to resolve Job DSL filter blocks in.
 */
public class TraceyFilterJobDslContext implements Context {
    List<TraceyFilter> filters = new ArrayList<>();

    /**
     * Configures a payload regex filter for the Tracey Trigger
     * @param regex The regex to configure
     */
    public void payloadRegex(String regex){
        filters.add(new EiffelPayloadRegexFilter(regex));
    }

    /**
     * Configures an Eiffel EventType filter
     * @param closure Closure containing EventTypeOption configuration
     */
    public void eiffelEventType(Closure closure) {
        TraceyEiffelEventTypeJobDslContext context = new TraceyEiffelEventTypeJobDslContext();
        ContextHelper.executeInContext(closure, context);
        filters.add(context.filter);
    }
}
