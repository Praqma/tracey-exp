package org.jenkinsci.tracey;

import javaposse.jobdsl.dsl.Context;
import net.praqma.tracey.broker.api.TraceyFilter;
import org.jenkinsci.tracey.filter.EiffelPayloadRegexFilter;

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

}
