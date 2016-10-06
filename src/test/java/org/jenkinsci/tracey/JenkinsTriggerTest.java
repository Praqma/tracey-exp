
package org.jenkinsci.tracey;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.ExtensionList;
import hudson.model.FreeStyleProject;
import net.praqma.tracey.broker.api.TraceyFilter;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQConnection;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQDefaults;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQRoutingInfo;
import net.praqma.tracey.broker.impl.rabbitmq.TraceyRabbitMQSenderImpl;
import org.jenkinsci.tracey.filter.TraceyPayloadRegexFilter;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class JenkinsTriggerTest {

    // Defaults
    private String exchange = "tracey";
    private String traceyHost = "localhost";
    private String envKey = "TRACEY_PAYLOAD";
    private String user = "test";
    private String password = "test";
    private int port = 6666;

    private int timeout = 60000;

    @Rule
    public JenkinsRule r =  new JenkinsRule();

    @Rule
    public TraceyRabbitMQServerRule sr = new TraceyRabbitMQServerRule(traceyHost, port);

    private UsernamePasswordCredentialsImpl createCredentials() {
        SystemCredentialsProvider scp = r.getInstance().getExtensionList(com.cloudbees.plugins.credentials.SystemCredentialsProvider.class).get(0);
        scp.getCredentials().add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, "test-id", "test-cred-description", user, password));
        UsernamePasswordCredentialsImpl upc = (UsernamePasswordCredentialsImpl)scp.getCredentials().get(0);
        return upc;
    }

    private FreeStyleProject createAndConfigureProject(JenkinsRule r, String jobName, UsernamePasswordCredentialsImpl creds) throws IOException {
        FreeStyleProject p = r.createFreeStyleProject(jobName);
        ExtensionList<TraceyGlobalConfig> t = r.getInstance().getExtensionList(TraceyGlobalConfig.class);
        TraceyGlobalConfig tgc = t.get(TraceyGlobalConfig.class);
        TraceyHost th = new TraceyHost(traceyHost, creds.getId(), "Test credential for test", port, "tracey-host-id");
        tgc.setConfiguredHosts(Arrays.asList(th));
        return p;
    }

    @Test
    public void givenFreeStyleJob_ShouldTrigger() throws Exception {
        // We assume that 'something' responds on port 6666. Ignored if pre-condition not met
        Assume.assumeThat(sr.isResponding(), is(true));

        UsernamePasswordCredentialsImpl creds = createCredentials();
        FreeStyleProject p = createAndConfigureProject(r, "Test_Trigger", creds);

        TraceyTrigger tt = new TraceyTrigger(exchange, creds.getId(), false, false, envKey, Collections.EMPTY_LIST);
        p.getTriggers().put(new TraceyTrigger.TraceyTriggerDescriptor(), tt);
        tt.start(p, true);

        TraceyRabbitMQSenderImpl sender = new TraceyRabbitMQSenderImpl(new RabbitMQConnection(traceyHost, port, user, password, RabbitMQDefaults.AUTOMATIC_RECOVERY));
        sender.send("Hi there we should trigger", new RabbitMQRoutingInfo());

        r.waitUntilNoActivityUpTo(timeout);

        TraceyAction ta = p.getBuildByNumber(1).getAction(TraceyAction.class);
        assertNotNull(ta);
        assertEquals("Hi there we should trigger", ta.getMetadata());
    }

    @Test
    public void givenFreeStyleJob_IgnoreMessageFilterActive() throws Exception {
        Assume.assumeThat(sr.isResponding(), is(true));

        UsernamePasswordCredentialsImpl creds = createCredentials();
        FreeStyleProject p = createAndConfigureProject(r, "Test_Ignore_PayloadFilter", creds);

        TraceyFilter tf = new TraceyPayloadRegexFilter(".*KEYWORD.*");

        TraceyTrigger tt = new TraceyTrigger(exchange, creds.getId(), false, false, envKey, Arrays.asList(tf));
        p.getTriggers().put(new TraceyTrigger.TraceyTriggerDescriptor(), tt);
        tt.start(p, true);

        TraceyRabbitMQSenderImpl sender = new TraceyRabbitMQSenderImpl(new RabbitMQConnection(traceyHost, 6666, user, password, RabbitMQDefaults.AUTOMATIC_RECOVERY));
        sender.send("Hi there we should trigger. Since we do not include the magic word", new RabbitMQRoutingInfo());
        sender.send("This one should be picked up. Since we do include the magic KEYWORD", new RabbitMQRoutingInfo());

        r.waitUntilNoActivityUpTo(timeout);

        TraceyAction ta = p.getBuildByNumber(1).getAction(TraceyAction.class);
        assertNotNull(ta);
        assertEquals("This one should be picked up. Since we do include the magic KEYWORD", ta.getMetadata());
    }

    @Test
    public void acceptMultilineRegexFromEiffelGeneration() throws Exception {

        Assume.assumeThat(sr.isResponding(), is(true));
        URI path = JenkinsTriggerTest.class.getResource("sourcechangeevent.json").toURI();
        String contents = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");

        UsernamePasswordCredentialsImpl creds = createCredentials();
        FreeStyleProject p = createAndConfigureProject(r, "Test_Accept_MultilineRegex", creds);

        TraceyFilter tf = new TraceyPayloadRegexFilter("(.*)EiffelSourceChangeCreatedEvent(.*)");
        assertNotNull("The post recieve hook should not reject this payload", tf.postReceive(contents));
        assertNull("This payload should be ignored", tf.postReceive("(.*)Mads(.*)"));

        TraceyTrigger tt = new TraceyTrigger(exchange, creds.getId(), false, false, envKey, Arrays.asList(tf));
        p.getTriggers().put(new TraceyTrigger.TraceyTriggerDescriptor(), tt);
        tt.start(p, true);

        TraceyRabbitMQSenderImpl sender = new TraceyRabbitMQSenderImpl(new RabbitMQConnection(traceyHost, 6666, user, password, RabbitMQDefaults.AUTOMATIC_RECOVERY));
        sender.send(contents, new RabbitMQRoutingInfo());

        r.waitUntilNoActivityUpTo(timeout);

        assertEquals(1, p._getRuns().size());

    }

}
