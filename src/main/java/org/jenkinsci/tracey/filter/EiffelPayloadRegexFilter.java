package org.jenkinsci.tracey.filter;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.praqma.tracey.broker.impl.filters.PayloadRegexFilter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EiffelPayloadRegexFilter extends PayloadRegexFilter implements Describable<EiffelPayloadRegexFilter> {

    private static final Logger LOG = Logger.getLogger(EiffelPayloadRegexFilter.class.getName());

    @DataBoundConstructor
    public EiffelPayloadRegexFilter(String regex) {
        super(regex);
    }

    @Override
    public String postReceive(String payload) {
        Matcher m = getRegexCompiled().matcher(payload);
        if(m.find()) {
            return payload;
        }
        LOG.info(String.format("Regex '%s' did not match anything in%n%s", getRegex(), payload));
        return null;
    }

    @Override
    public Descriptor<EiffelPayloadRegexFilter> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension
    public static class EiffelPayloadRegexFilterDescriptor extends Descriptor<EiffelPayloadRegexFilter> {

        public static String DEFAULT_REGEX = ".*";

        @Override
        public String getDisplayName() {
            return "Payload filter";
        }

        public FormValidation doCheckRegex(@QueryParameter String regex) {
            try {
                Pattern p = Pattern.compile(regex);
                return FormValidation.ok("Regex looks ok");
            } catch (Exception wrongPattern) {
                return FormValidation.error(wrongPattern, "Error while parsing pattern");
            }
        }

    }

}
