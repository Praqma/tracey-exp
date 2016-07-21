
package org.jenkinsci.tracey;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.ExtensionList;
import hudson.model.FreeStyleProject;
import java.util.Arrays;
import java.util.Collections;
import net.praqma.tracey.broker.rabbitmq.TraceyFilter;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQSenderImpl;
import org.jenkinsci.tracey.filter.EiffelPayloadRegexFilter;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Assume;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JenkinsTriggerTests {



    // Defaults
    private String exchange = "tracey";
    private String traceyHost = "localhost";
    private String envKey = "TRACEY_PAYLOAD";
    private String user = "test";
    private String password = "test";
    private int port = 6666;

    @Rule
    public JenkinsRule r =  new JenkinsRule();

    @Rule
    public TraceyRabbitMQServerRule sr = new TraceyRabbitMQServerRule(traceyHost, port);

    @Test
    public void givenFreeStyleJob_ShouldTrigger() throws Exception {
        // We assume that 'something' responds on port 6666. Ignored if pre-condition not met
        Assume.assumeThat(sr.isResponding(), is(true));

        FreeStyleProject p = r.createFreeStyleProject("Test_Receive");
        SystemCredentialsProvider scp = r.getInstance().getExtensionList(com.cloudbees.plugins.credentials.SystemCredentialsProvider.class).get(0);
        scp.getCredentials().add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, "test-id", "test-cred-description", "test", "test"));
        UsernamePasswordCredentialsImpl upc = (UsernamePasswordCredentialsImpl)scp.getCredentials().get(0);

        ExtensionList<TraceyGlobalConfig> t = r.getInstance().getExtensionList(TraceyGlobalConfig.class);
        TraceyGlobalConfig tgc = t.get(TraceyGlobalConfig.class);
        TraceyHost th = new TraceyHost(traceyHost, upc.getId(), "Test credential for test", 6666);
        tgc.setConfiguredHosts(Arrays.asList(th));


        TraceyTrigger tt = new TraceyTrigger(exchange, upc.getId(), false, false, envKey, Collections.EMPTY_LIST);
        p.getTriggers().put(new TraceyTrigger.TraceyTriggerDescriptor(), tt);
        tt.start(p, true);


        TraceyRabbitMQSenderImpl sender = new TraceyRabbitMQSenderImpl(traceyHost, user, password, 6666);
        sender.send("Hi there we should trigger", "tracey");

        r.waitUntilNoActivity();

        TraceyAction ta = p.getBuildByNumber(1).getAction(TraceyAction.class);
        assertNotNull(ta);
        assertEquals("Hi there we should trigger", ta.getMetadata());
    }

    @Test
    public void givenFreeStyleJob_IgnoreMessageFilterActive() throws Exception {
        Assume.assumeThat(sr.isResponding(), is(true));
        FreeStyleProject p = r.createFreeStyleProject("Test_Ignore_PayloadFilter");
        SystemCredentialsProvider scp = r.getInstance().getExtensionList(com.cloudbees.plugins.credentials.SystemCredentialsProvider.class).get(0);
        scp.getCredentials().add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, "test-id", "test-cred-description", "test", "test"));
        UsernamePasswordCredentialsImpl upc = (UsernamePasswordCredentialsImpl)scp.getCredentials().get(0);

        ExtensionList<TraceyGlobalConfig> t = r.getInstance().getExtensionList(TraceyGlobalConfig.class);
        TraceyGlobalConfig tgc = t.get(TraceyGlobalConfig.class);
        TraceyHost th = new TraceyHost(traceyHost, upc.getId(), "Test credential for test", 6666);
        tgc.setConfiguredHosts(Arrays.asList(th));

        TraceyFilter tf = new EiffelPayloadRegexFilter(".*KEYWORD.*");

        TraceyTrigger tt = new TraceyTrigger(exchange, upc.getId(), false, false, envKey, Arrays.asList(tf));
        p.getTriggers().put(new TraceyTrigger.TraceyTriggerDescriptor(), tt);
        tt.start(p, true);


        TraceyRabbitMQSenderImpl sender = new TraceyRabbitMQSenderImpl(traceyHost, user, password, 6666);
        sender.send("Hi there we should trigger. Since we do not include the magic word", "tracey");
        sender.send("This one should be picked up. Since we do include the magic KEYWORD", "tracey");

        r.waitUntilNoActivity();

        TraceyAction ta = p.getBuildByNumber(1).getAction(TraceyAction.class);
        assertNotNull(ta);
        assertEquals("This one should be picked up. Since we do include the magic KEYWORD", ta.getMetadata());

    }

}
