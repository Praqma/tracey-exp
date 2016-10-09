package org.jenkinsci.tracey.filter;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import net.praqma.tracey.broker.impl.filters.PayloadRegexFilter;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.logging.Logger;

public class PayloadJSONBasicFilter extends net.praqma.tracey.broker.impl.filters.PayloadJSONFilter implements Describable<PayloadJSONBasicFilter> {
    private static final Logger LOG = Logger.getLogger(PayloadRegexFilter.class.getName());

    @DataBoundConstructor
    public PayloadJSONBasicFilter(String key, String value) {
        super(key, value);
    }

    @Override
    public Descriptor<PayloadJSONBasicFilter> getDescriptor() {

        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension
    public static class PayloadJSONBasicFilterDescriptor extends Descriptor<PayloadJSONBasicFilter> {

        public static String DEFAULT_KEY = "name";
        public static String DEFAULT_VALUE = "value";
        @Override
        public String getDisplayName() {
            return "JSON Basic filter";
        }

    }
}
