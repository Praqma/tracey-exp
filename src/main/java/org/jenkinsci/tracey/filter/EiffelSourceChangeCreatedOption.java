package org.jenkinsci.tracey.filter;

import hudson.Extension;
import hudson.model.Descriptor;
import net.praqma.tracey.broker.rabbitmq.TraceyEventTypeFilter;
import org.kohsuke.stapler.DataBoundConstructor;

public class EiffelSourceChangeCreatedOption extends EiffelEventTypeOption {

    @DataBoundConstructor
    public EiffelSourceChangeCreatedOption() { }

    @Override
    public String getFilterClassName() {
        return TraceyEventTypeFilter.getClassNameForEiffelSourceChangeCreatedEvent();
    }

    @Override
    public Descriptor<EiffelEventTypeOption> getDescriptor() {
        return new EiffelSourceChangeCreatedOptionDescriptor();
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
            return TraceyEventTypeFilter.getClassNameForEiffelSourceChangeCreatedEvent().split("\\$")[1];
        }

    }

}
