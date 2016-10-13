package org.jenkinsci.tracey;

import javaposse.jobdsl.dsl.Context;
import net.praqma.tracey.broker.api.TraceyFilter;
import org.jenkinsci.tracey.filter.PayloadJSONBasicFilter;
import org.jenkinsci.tracey.filter.PayloadJSONRegexFilter;
import org.jenkinsci.tracey.filter.TraceyPayloadRegexFilter;

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
        filters.add(new TraceyPayloadRegexFilter(regex));
    }
    public void payloadKeyValue(String key, String value){
        filters.add(new PayloadJSONBasicFilter(key, value));
    }
    public void payloadJSONRegex(String pattern) {filters.add(new PayloadJSONRegexFilter(pattern));}
}
