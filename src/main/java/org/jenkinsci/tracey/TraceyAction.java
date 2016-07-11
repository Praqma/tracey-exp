package org.jenkinsci.tracey;

import hudson.model.Action;

/**
 *
 * @author Mads
 */
public class TraceyAction implements Action {

    private String metadata;
    private String envKey;
    private boolean contribute;

    public TraceyAction(String metadata, String envKey, boolean contribute) {
        this.metadata = metadata;
        this.envKey = envKey;
        this.contribute = contribute;
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

    /**
     * @return the envKey
     */
    public String getEnvKey() {
        return envKey;
    }

    /**
     * @param envKey the envKey to set
     */
    public void setEnvKey(String envKey) {
        this.envKey = envKey;
    }

    /**
     * @return the contribute
     */
    public boolean isContribute() {
        return contribute;
    }

    /**
     * @param contribute the contribute to set
     */
    public void setContribute(boolean contribute) {
        this.contribute = contribute;
    }

}
