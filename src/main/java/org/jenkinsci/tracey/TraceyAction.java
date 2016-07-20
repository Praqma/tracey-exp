package org.jenkinsci.tracey;

import hudson.model.Action;

public class TraceyAction implements Action {

    private String metadata;
    private String envKey;

    public TraceyAction(String metadata, String envKey) {
        this.metadata = metadata;
        this.envKey = envKey;
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
}
