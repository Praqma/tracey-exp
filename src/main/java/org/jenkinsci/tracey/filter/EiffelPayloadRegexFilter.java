package org.jenkinsci.tracey.filter;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import net.praqma.tracey.broker.rabbitmq.PayloadRegexFilter;
import org.kohsuke.stapler.DataBoundConstructor;

public class EiffelPayloadRegexFilter extends PayloadRegexFilter implements Describable<EiffelPayloadRegexFilter> {

    private static final Logger LOG = Logger.getLogger(EiffelPayloadRegexFilter.class.getName());

    @DataBoundConstructor
    public EiffelPayloadRegexFilter(String regex) {
        super(regex);
    }


    @Override
    public String postReceive(String payload) {
        Matcher m = getRegexCompiled().matcher(payload);
        if(m.matches()) {
            return payload;
        }
        LOG.info(String.format("Regex '%s' did not match anything in%n%s", getRegex(), payload));
        return null;
    }



    @Override
    public Descriptor<EiffelPayloadRegexFilter> getDescriptor() {
        return new EiffelPayloadRegexFilterDescriptor();
    }

    @Extension
    public static class EiffelPayloadRegexFilterDescriptor extends Descriptor<EiffelPayloadRegexFilter> {

        public static String DEFAULT_REGEX = ".*";

        @Override
        public String getDisplayName() {
            return "Payload filter";
        }

    }

}
