/*
 * Copyright (C) ThermoFisher Scientific Inc.- All Rights Reserved
 * Unauthorized use or copying of this file, via any medium is strictly prohibited and will be subject to legal action.
 * Proprietary and confidential
 *
 */
package com.rabbitmq.guice.examples.exchangetypes;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.guice.examples.configuration.ConnectionManager;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class DirectExchange {

    public static void declareExchange() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        //Declare my-direct-exchange DIRECT exchange
        channel.exchangeDeclare("my-direct-exchange", BuiltinExchangeType.DIRECT, true);
        channel.close();
    }

    public static void declareQueues() throws IOException, TimeoutException {

        Channel channel = ConnectionManager.getConnection().createChannel();
        //queueDeclare  - (queueName, durable, exclusive, autoDelete, arguments)
        channel.queueDeclare("MobileQ", true, false, false, null);
        channel.queueDeclare("ACQ", true, false, false, null);
        channel.queueDeclare("LightQ", true, false, false, null);
        channel.close();
    }

    public static void declareBindings() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        //Create bindings - (queue, exchange, routingKey)
        channel.queueBind("MobileQ", "my-direct-exchange", "personalDevice");
        channel.queueBind("ACQ", "my-direct-exchange", "homeAppliance");
        channel.queueBind("LightQ", "my-direct-exchange", "homeAppliance");
        channel.close();
    }

    public static void subscribeMessage() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();

        channel.basicConsume("LightQ", true, ((consumerTag, message) -> {
            System.out.println(consumerTag);
            System.out.println("LightQ:" + new String(message.getBody()));
        }), System.out::println);

        channel.basicConsume("ACQ", true, ((consumerTag, message) -> {
            System.out.println(consumerTag);
            System.out.println("ACQ:" + new String(message.getBody()));
        }), System.out::println);

        channel.basicConsume("MobileQ", true, ((consumerTag, message) -> {
            System.out.println(consumerTag);
            System.out.println("MobileQ:" + new String(message.getBody()));
        }), System.out::println);

        channel.close();
    }

    //Publish the message
    public static void publishMessage() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        String message = "Direct message - Turn on the Home Appliances ";
        channel.basicPublish("my-direct-exchange", "personalDevice", null, message.getBytes());
        channel.close();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        DirectExchange.declareQueues();
        DirectExchange.declareExchange();
        DirectExchange.declareBindings();

        Thread subscribe = new Thread(() -> {
            try {
                DirectExchange.subscribeMessage();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        });

        Thread publish = new Thread(() -> {
            try {
                DirectExchange.publishMessage();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        });

        subscribe.start();
        publish.start();
    }
}
