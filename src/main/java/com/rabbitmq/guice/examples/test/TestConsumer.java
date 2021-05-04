/*
 * Copyright (C) ThermoFisher Scientific Inc.- All Rights Reserved
 * Unauthorized use or copying of this file, via any medium is strictly prohibited and will be subject to legal action.
 * Proprietary and confidential
 *
 */
package com.rabbitmq.guice.examples.test;

import com.rabbitmq.client.*;
import com.rabbitmq.guice.constants.CommonConfigs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class TestConsumer {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection(CommonConfigs.AMQP_URL);
        Channel channel = connection.createChannel();
        DeliverCallback deliverCallback = (consumerTag, message) -> {
            System.out.println(consumerTag);
            System.out.println(new String(message.getBody(), StandardCharsets.UTF_8));
        };
        CancelCallback cancelCallback = consumerTag -> {
            System.out.println(consumerTag);
        };
        channel.basicConsume("LightQ", true, deliverCallback, cancelCallback);
    }
}
