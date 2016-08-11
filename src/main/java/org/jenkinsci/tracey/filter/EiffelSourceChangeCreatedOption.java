package org.jenkinsci.tracey.filter;

import hudson.Extension;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

public class EiffelSourceChangeCreatedOption extends EiffelEventTypeOption {

    static private String FILTER_CLASS = "EiffelSourceChangeCreatedEvent";

    @DataBoundConstructor
    public EiffelSourceChangeCreatedOption() { }

    @Override
    public String getFilterClassName() {
        return FILTER_CLASS;
    }

    @Override
    public Descriptor<EiffelEventTypeOption> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension
    public static class EiffelSourceChangeCreatedOptionDescriptor extends Descriptor<EiffelEventTypeOption> {

        /**
         *
         * @return  a display name for this option. Currently we use the inner-class
         *          name for the name of the event.
         */
        @Override
        public String getDisplayName() {
            return FILTER_CLASS;
        }

    }

}
