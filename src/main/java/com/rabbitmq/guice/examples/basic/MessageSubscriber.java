/*
 * Copyright (C) ThermoFisher Scientific Inc.- All Rights Reserved
 * Unauthorized use or copying of this file, via any medium is strictly prohibited and will be subject to legal action.
 * Proprietary and confidential
 *
 */
package com.rabbitmq.guice.examples.basic;

import com.rabbitmq.client.*;
import com.rabbitmq.guice.constants.CommonConfigs;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MessageSubscriber {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection(CommonConfigs.AMQP_URL);
        Channel channel = connection.createChannel();
        DeliverCallback deliverCallback = (s, delivery) -> {
            System.out.println(new String(delivery.getBody()));
        };
        CancelCallback cancelCallback = System.out::println;
        channel.basicConsume(CommonConfigs.DEFAULT_QUEUE, true, deliverCallback, cancelCallback);
        channel.close();
        connection.close();

    }
}
