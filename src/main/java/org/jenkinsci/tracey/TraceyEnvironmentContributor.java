package org.jenkinsci.tracey;

import static jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.triggers.Trigger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.json.JSONObject;
import net.praqma.tracey.broker.rabbitmq.TraceyEiffelMessageValidator;
import org.apache.commons.lang.StringUtils;
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
        TraceyAction tAction = r.getAction(TraceyAction.class);
        TraceyTrigger t = findTriggerForRun(r);
        if (t != null) {
            if(t.isInjectEnvironment() && tAction != null) {
                LOG.info(String.format("Contributed environment with key: %s", tAction.getEnvKey()));
                LOG.info(String.format("Contributed environment with value: %s", tAction.getMetadata()));

                envs.put(tAction.getEnvKey(), tAction.getMetadata());

                try {
                    HashMap<String,Pattern> p = validateRegex(t.getRegexToEnv());
                    LOG.info(String.format("Found %s valid patterns", p.size()));
                    envs.putAll(findEnvValues(p, tAction.getMetadata()));
                } catch (PatternSyntaxException ex) {
                    LOG.log(Level.WARNING, "Syntax error in regex detected", ex);
                }

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
        } else {
            LOG.info(String.format("Trigger project not compatible for project type %s",
                    r.getParent().getClass().getSimpleName()));
        }
        return null;
    }

    public static HashMap<String,Pattern> validateRegex(String regex) throws PatternSyntaxException {
        HashMap<String,Pattern> p = new HashMap<String, Pattern>();
        if(!StringUtils.isBlank(regex)) {
            String[] lines = regex.split("[\\r\\n]+");
            LOG.fine(String.format("Found %s lines in configuration", lines.length));
            for(String line : lines) {
                String[] comp = line.split("\\s");
                if(comp.length == 2) {
                    String key = comp[0];
                    String rx = comp[1];
                    Pattern pat = Pattern.compile(rx);
                    LOG.fine(String.format("Added regex %s", pat));
                    p.put(key, pat);
                }
            }
        }
        return p;
    }

    public static HashMap<String,String> findEnvValues(HashMap<String,Pattern> patterns, String payload) {
        HashMap<String,String> envValues = new HashMap<String, String>();
        for(Entry<String,Pattern> s : patterns.entrySet()) {
            Matcher m = s.getValue().matcher(payload);
            while(m.find()) {
                envValues.put(s.getKey(), m.group(1));
                break;
            }
        }
        return envValues;
    }

}
