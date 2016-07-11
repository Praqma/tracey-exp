package org.jenkinsci.tracey;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * I decided to go with an environment contributor instead of an action for workflow compatability
 * See this: http://stackoverflow.com/questions/31326286/can-a-workflow-step-access-environment-variables-provided-by-an-environmentcontr
 */
@Extension
public class TraceyEnvironmentContributor extends EnvironmentContributor {

    private static final Logger LOG = Logger.getLogger(TraceyEnvironmentContributor.class.getName());

    @Override
    public void buildEnvironmentFor(Run r, EnvVars envs, TaskListener listener) throws IOException, InterruptedException {
        super.buildEnvironmentFor(r, envs, listener); //To change body of generated methods, choose Tools | Templates.
        LOG.info("Contributing tracey environment");
        if (r.getAction(TraceyAction.class) != null) {
            TraceyAction tac = r.getAction(TraceyAction.class);
            if(tac.isContribute()) {
                LOG.info("Contributed environment to key: "+tac.getEnvKey());
                envs.put(tac.getEnvKey(), tac.getMetadata());
            }
        }
    }
}
