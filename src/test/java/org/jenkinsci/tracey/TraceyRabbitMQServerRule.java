package org.jenkinsci.tracey;

import java.io.IOException;
import java.net.Socket;
import org.junit.rules.ExternalResource;

/**
 *
 * @author Mads
 */
public class TraceyRabbitMQServerRule extends ExternalResource {

    private int port;
    private String host;

    public TraceyRabbitMQServerRule(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public boolean isResponding() {
        try (Socket ignored = new Socket(host, port)) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

}
