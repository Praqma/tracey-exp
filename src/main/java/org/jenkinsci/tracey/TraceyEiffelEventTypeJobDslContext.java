package org.jenkinsci.tracey;

import javaposse.jobdsl.dsl.Context;
import org.jenkinsci.tracey.filter.EiffelArtifactPublishedOption;
import org.jenkinsci.tracey.filter.EiffelEventTypeFilter;
import org.jenkinsci.tracey.filter.EiffelSourceChangeCreatedOption;

import java.util.ArrayList;

public class TraceyEiffelEventTypeJobDslContext implements Context {
    public EiffelEventTypeFilter filter = new EiffelEventTypeFilter(new ArrayList<>());

    /**
     * Adds the 'ArtifactPublished' event to the filter.
     */
    public void artifactPublished() {
        filter.getAcceptEventTypes().add(new EiffelArtifactPublishedOption());
    }

    /**
     * Adds the 'SourceChangeCreated' event to the filter.
     */
    public void sourceChangeCreated() {
        filter.getAcceptEventTypes().add(new EiffelSourceChangeCreatedOption());
    }
}
