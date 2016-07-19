package org.jenkinsci.tracey;

import java.util.HashMap;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EnvironmentContributionTest {
    
    String envConf = "ONE one:(\\S+)\nTWO two:(\\S+)";
    String input = "This is value we want to capture: one:one two:two";

    @Test
    public void testCorrectFoundValues() {
        HashMap<String,Pattern> patterns = TraceyEnvironmentContributor.validateRegex(envConf);
        assertTrue(patterns.containsKey("ONE"));
        assertTrue(patterns.containsKey("TWO"));
    }

    @Test
    public void testCorrectSubstitution() {
        HashMap<String,Pattern> patterns = TraceyEnvironmentContributor.validateRegex(envConf);
        HashMap<String,String> values = TraceyEnvironmentContributor.findEnvValues(patterns, input);
        assertEquals("one", values.get("ONE"));
        assertEquals("two", values.get("TWO"));
    }
}
