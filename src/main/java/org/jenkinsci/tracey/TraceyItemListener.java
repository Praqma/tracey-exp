package org.jenkinsci.tracey;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import hudson.triggers.Trigger;
import java.util.logging.Logger;
import jenkins.model.ParameterizedJobMixIn;


@Extension
public class TraceyItemListener extends ItemListener {

    private static final Logger LOG = Logger.getLogger(TraceyItemListener.class.getName());

    @Override
    public void onDeleted(Item item) {
        super.onDeleted(item);
        if(item instanceof ParameterizedJobMixIn.ParameterizedJob) {
            ParameterizedJobMixIn.ParameterizedJob mixin = (ParameterizedJobMixIn.ParameterizedJob)item;
            for(Trigger<?> t :  mixin.getTriggers().values()) {
                if(t instanceof TraceyTrigger) {
                    TraceyTrigger tt = (TraceyTrigger)t;
                    tt.stop();
                    LOG.info(String.format("Stopped TraceyTrigger for job: %s", mixin.getName()));
                }
            }
        }
    }

}
