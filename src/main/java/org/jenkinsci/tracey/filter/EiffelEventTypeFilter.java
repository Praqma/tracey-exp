package org.jenkinsci.tracey.filter;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.Jenkins;

import net.praqma.tracey.broker.rabbitmq.TraceyEventTypeFilter;
import org.kohsuke.stapler.DataBoundConstructor;


public class EiffelEventTypeFilter extends TraceyEventTypeFilter implements Describable<EiffelEventTypeFilter> {

    private List<EiffelEventTypeOption> acceptEventTypes = new ArrayList<EiffelEventTypeOption>();

    @DataBoundConstructor
    public EiffelEventTypeFilter(List<EiffelEventTypeOption> acceptEventTypes) throws ClassNotFoundException {
        super(createRules(acceptEventTypes));
        this.acceptEventTypes = acceptEventTypes;
    }

    @Override
    public Descriptor<EiffelEventTypeFilter> getDescriptor() {
        return new EiffelEventTypeFilterDescriptor();
    }

    private static String[] createRules(List<EiffelEventTypeOption> options) {
        if(options != null) {
            String[] optString = new String[options.size()];
            int i = 0;

            for(EiffelEventTypeOption opt : options) {
                optString[i] = opt.getFilterClassName();
                i++;
            }
            return optString;
        }
        return null;
    }

    /**
     * @return the acceptEventTypes
     */
    public List<EiffelEventTypeOption> getAcceptEventTypes() {
        return acceptEventTypes;
    }

    /**
     * @param acceptEventTypes the acceptEventTypes to set
     */
    public void setAcceptEventTypes(List<EiffelEventTypeOption> acceptEventTypes) {
        this.acceptEventTypes = acceptEventTypes;
    }

    @Extension
    public static class EiffelEventTypeFilterDescriptor extends Descriptor<EiffelEventTypeFilter> {

        public EiffelEventTypeFilterDescriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Event type filter";
        }

        public static DescriptorExtensionList<EiffelEventTypeOption, Descriptor<EiffelEventTypeOption>> allOptions() {
            return Jenkins.getActiveInstance().getDescriptorList(EiffelEventTypeOption.class);
        }

    }
}
