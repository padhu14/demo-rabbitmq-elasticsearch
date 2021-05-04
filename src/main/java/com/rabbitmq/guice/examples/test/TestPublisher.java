/*
 * Copyright (C) ThermoFisher Scientific Inc.- All Rights Reserved
 * Unauthorized use or copying of this file, via any medium is strictly prohibited and will be subject to legal action.
 * Proprietary and confidential
 *
 */
package com.rabbitmq.guice.examples.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.guice.constants.CommonConfigs;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TestPublisher {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection(CommonConfigs.AMQP_URL);
        Channel channel = connection.createChannel();
        //Basic publish
        String message = "Turn on home appliances";
        // publish with (exchange, routingKey, properties, messageBody)
        channel.basicPublish("my-direct-exchange", "homeAppliance", null, message.getBytes());
        //Close the channel and connection
        channel.close();
        connection.close();
    }
}
