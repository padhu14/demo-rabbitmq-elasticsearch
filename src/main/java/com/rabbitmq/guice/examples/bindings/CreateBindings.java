/*
 * Copyright (C) ThermoFisher Scientific Inc.- All Rights Reserved
 * Unauthorized use or copying of this file, via any medium is strictly prohibited and will be subject to legal action.
 * Proprietary and confidential
 *
 */
package com.rabbitmq.guice.examples.bindings;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.guice.constants.CommonConfigs;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class CreateBindings {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        Connection connection = connectionFactory.newConnection(CommonConfigs.AMQP_URL);
        try (Channel channel = connection.createChannel()) {
            //Create bindings - (queue, exchange, routingKey)
            channel.queueBind("MobileQ", "my-direct-exchange", "personalDevice");
            channel.queueBind("ACQ", "my-direct-exchange", "homeAppliance");
            channel.queueBind("LightQ", "my-direct-exchange", "homeAppliance");
        }
        connection.close();
    }
}
