package org.jenkinsci.tracey;

import static jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.triggers.Trigger;
import java.io.IOException;
import java.util.logging.Logger;
import org.json.JSONObject;
import net.praqma.tracey.broker.rabbitmq.TraceyEiffelMessageValidator;

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
        TraceyAction tAction = r.getAction(TraceyAction.class);
        TraceyTrigger t = findTriggerForRun(r);
        if (t != null) {
            LOG.info("Contributing tracey environment for job "+r.getParent().getName());
            if(t.isInjectEnvironment() && tAction != null) {
                LOG.info("Contributed environment with key: "+tAction.getEnvKey());
                envs.put(tAction.getEnvKey(), tAction.getMetadata());

                if(t.isGitReady()) {
                    JSONObject git = TraceyEiffelMessageValidator.getGitIdentifier(tAction.getMetadata());
                    if (git != null) {

                        String branch = git.getString("branch");
                        String repoName = git.getString("repoName");
                        String commitId = git.getString("commitId");
                        String repoUri = git.getString("repoUri");

                        envs.put("GIT_COMMIT", commitId);
                        envs.put("GIT_BRANCH_TRACEY", branch);
                        envs.put("GIT_URL_TRACEY", repoUri);
                        envs.put("GIT_REPO_NAME_TRACEY", repoName);

                        LOG.info("GIT_COMMIT_TRACEY = "+commitId);
                        LOG.info("GIT_BRANCH_TRACEY= "+branch);
                        LOG.info("GIT_URL_TRACEY= "+repoUri);
                        LOG.info("GIT_REPO_TRACEY = "+repoName);
                    }
                }
            }
        }
    }

    private TraceyTrigger findTriggerForRun(Run r) {
        if (r.getParent() instanceof ParameterizedJob) {
            ParameterizedJob jobP = (ParameterizedJob)r.getParent();
            for(Trigger<?> trigs : jobP.getTriggers().values()) {
                if(trigs instanceof TraceyTrigger) {
                    return (TraceyTrigger)trigs;
                }
            }
        } else {
            LOG.info("Trigger project not compatible");
        }
        return null;
    }
}
