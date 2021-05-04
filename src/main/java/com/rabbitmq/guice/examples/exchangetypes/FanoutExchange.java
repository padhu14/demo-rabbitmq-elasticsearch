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

public class FanoutExchange {

    public static void declareExchange() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        //Declare my-fanout-exchange
        channel.exchangeDeclare("my-fanout-exchange", BuiltinExchangeType.FANOUT, true);
        channel.close();
    }

    public static void declareQueues() throws IOException, TimeoutException {
        //Create a channel - do no't share the Channel instance
        Channel channel = ConnectionManager.getConnection().createChannel();
        //Create the Queues
        channel.queueDeclare("MobileQ", true, false, false, null);
        channel.queueDeclare("ACQ", true, false, false, null);
        channel.queueDeclare("LightQ", true, false, false, null);
        channel.close();
    }

    public static void declareBindings() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        //Create bindings - (queue, exchange, routingKey) - routingKey != null
        channel.queueBind("MobileQ", "my-fanout-exchange", "");
        channel.queueBind("ACQ", "my-fanout-exchange", "");
        channel.queueBind("LightQ", "my-fanout-exchange", "");
        channel.close();
    }

    public static void subscribeMessage() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        channel.basicConsume("LightQ", true, ((consumerTag, message) -> {
            System.out.println(consumerTag);
            System.out.println("LightQ: " + new String(message.getBody()));
        }), System.out::println);
        channel.basicConsume("ACQ", true, ((consumerTag, message) -> {
            System.out.println(consumerTag);
            System.out.println("ACQ: " + new String(message.getBody()));
        }), System.out::println);
        channel.basicConsume("MobileQ", true, ((consumerTag, message) -> {
            System.out.println(consumerTag);
            System.out.println("MobileQ: " + new String(message.getBody()));
        }), consumerTag -> {
            System.out.println(consumerTag);
        });
    }

    public static void publishMessage() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        String message = "Main Power is ON";
        channel.basicPublish("my-fanout-exchange", "", null, message.getBytes());
        channel.close();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        FanoutExchange.declareQueues();
        FanoutExchange.declareExchange();
        FanoutExchange.declareBindings();
        //Threads created to publish-subscribe asynchronously
        Thread subscribe = new Thread(() -> {
            try {
                FanoutExchange.subscribeMessage();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        });
        Thread publish = new Thread(() -> {
            try {
                FanoutExchange.publishMessage();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        });
        subscribe.start();
        publish.start();
    }
}
