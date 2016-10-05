package org.jenkinsci.tracey;

import org.jenkinsci.tracey.filter.TraceyPayloadRegexFilter;
import org.junit.Test;

import static org.junit.Assert.*;

public class EiffelPayloadRegexTest {

    @Test
    public void accept() {
        String msg = "mads is cool";
        TraceyPayloadRegexFilter madsFilter = new TraceyPayloadRegexFilter(".*mads.*");
        String response =  madsFilter.postReceive(msg);
        assertNotNull(response);
        assertEquals("mads is cool", response);
    }

    @Test
    public void reject() {
        String msg = "reject this message";
        TraceyPayloadRegexFilter madsFilter = new TraceyPayloadRegexFilter(".*mads.*");
        String response =  madsFilter.postReceive(msg);
        assertNull(response);
    }
    
}
