package org.jenkinsci.tracey;

import hudson.model.Action;

/**
 *
 * @author Mads
 */
public class TraceyAction implements Action {

    private String metadata;

    public TraceyAction(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Tracey Meta Data";
    }

    @Override
    public String getUrlName() {
        return "tracey";
    }

    /**
     * @return the metadata
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

}
