package org.jenkinsci.tracey.filter;

import hudson.model.Describable;
import hudson.model.Descriptor;

public abstract class EiffelEventTypeOption implements Describable<EiffelEventTypeOption> {

    public abstract String getFilterClassName();

    @Override
    public Descriptor<EiffelEventTypeOption> getDescriptor() {
        return new EiffelEventTypeOptionDescriptor();
    }

    public static class EiffelEventTypeOptionDescriptor extends Descriptor<EiffelEventTypeOption> {

        @Override
        public String getDisplayName() {
            return "";
        }

    }

}
