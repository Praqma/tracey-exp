package org.jenkinsci.tracey;

import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQConnection;

public class RabbitMQConnectionHolder {

    private static RabbitMQConnection connection;

    public static RabbitMQConnection createConnection(final String host, final int port, final String userName, final String password, final Boolean automaticRecovery) {
        return connection = new RabbitMQConnection(host, port, userName, password, automaticRecovery);
    }

    public static RabbitMQConnection getConnection() {
        return connection;
    }
}
