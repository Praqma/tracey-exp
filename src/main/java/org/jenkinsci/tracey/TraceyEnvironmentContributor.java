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
        super.buildEnvironmentFor(r, envs, listener);
        LOG.info(String.format("Started Tracey environment for job: %s", r.getParent().getName()));
        TraceyAction tAction = r.getAction(TraceyAction.class);
        TraceyTrigger t = findTriggerForRun(r);
        if (t != null) {
            if(t.isInjectEnvironment() && tAction != null) {
                LOG.info(String.format("Contributed environment with key: %s", tAction.getEnvKey()));
                envs.put(tAction.getEnvKey(), tAction.getMetadata());

                if(t.isGitReady()) {
                    JSONObject git = TraceyEiffelMessageValidator.getGitIdentifier(tAction.getMetadata());
                    if (git != null) {
                        envs.put("GIT_COMMIT_TRACEY", git.getString("commitId"));
                        envs.put("GIT_BRANCH_TRACEY", git.getString("branch"));
                        envs.put("GIT_URL_TRACEY", git.getString("repoUri"));
                        envs.put("GIT_REPO_NAME_TRACEY", git.getString("repoName"));
                    }
                }
            } else {
                LOG.info("Tracey environment contribution disabled");
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
            LOG.info(String.format("No TraceyTrigger found in job: %s", r.getParent().getName()));
        } else {
            LOG.info("Trigger project not compatible");
        }
        return null;
    }
}
