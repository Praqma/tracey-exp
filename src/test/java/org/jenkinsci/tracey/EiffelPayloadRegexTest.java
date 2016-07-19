package org.jenkinsci.tracey;

import org.jenkinsci.tracey.filter.EiffelPayloadRegexFilter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class EiffelPayloadRegexTest {

    @Test
    public void accept() {
        String msg = "mads is cool";
        EiffelPayloadRegexFilter madsFilter = new EiffelPayloadRegexFilter(".*mads.*");
        String response =  madsFilter.postReceive(msg);
        assertNotNull(response);
        assertEquals("mads is cool", response);
    }

    @Test
    public void reject() {
        String msg = "reject this message";
        EiffelPayloadRegexFilter madsFilter = new EiffelPayloadRegexFilter(".*mads.*");
        String response =  madsFilter.postReceive(msg);
        assertNull(response);
    }
    
}
