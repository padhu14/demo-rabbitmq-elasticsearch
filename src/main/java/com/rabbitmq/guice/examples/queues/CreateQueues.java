/*
 * Copyright (C) ThermoFisher Scientific Inc.- All Rights Reserved
 * Unauthorized use or copying of this file, via any medium is strictly prohibited and will be subject to legal action.
 * Proprietary and confidential
 *
 */
package com.rabbitmq.guice.examples.queues;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.guice.constants.CommonConfigs;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class CreateQueues {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection(CommonConfigs.AMQP_URL);
        Channel channel = connection.createChannel();
        //Create the Queues
        channel.queueDeclare("MobileQ", true, false, false, null);
        channel.queueDeclare("ACQ", true, false, false, null);
        channel.queueDeclare("LightQ", true, false, false, null);
        channel.queueDeclare(CommonConfigs.DEFAULT_QUEUE, true, false, false, null);
        channel.close();
        connection.close();
    }
}
