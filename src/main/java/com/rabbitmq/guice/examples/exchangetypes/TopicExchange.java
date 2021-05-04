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

public class TopicExchange {

    public static void declareExchange() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        //Create Topic Exchange
        channel.exchangeDeclare("my-topic-exchange", BuiltinExchangeType.TOPIC, true);
        channel.close();
    }

    public static void declareQueues() throws IOException, TimeoutException {
        //Create a channel - do not share the Channel instance
        Channel channel = ConnectionManager.getConnection().createChannel();
        //Create the Queues
        channel.queueDeclare("HealthQ", true, false, false, null);
        channel.queueDeclare("SportsQ", true, false, false, null);
        channel.queueDeclare("EducationQ", true, false, false, null);
        channel.close();
    }

    public static void declareBindings() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        //Create bindings - (queue, exchange, routingKey) - routingKey != null
        channel.queueBind("HealthQ", "my-topic-exchange", "health.*");
        channel.queueBind("SportsQ", "my-topic-exchange", "#.sports.*");
        channel.queueBind("EducationQ", "my-topic-exchange", "#.education");
        channel.close();
    }

    public static void subscribeMessage() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        channel.basicConsume("HealthQ", true, ((consumerTag, message) -> {
            System.out.println("\n\n=========== Health Queue ==========");
            System.out.println(consumerTag);
            System.out.println("HealthQ: " + new String(message.getBody()));
            System.out.println(message.getEnvelope());
        }), consumerTag -> {
            System.out.println(consumerTag);
        });
        channel.basicConsume("SportsQ", true, ((consumerTag, message) -> {
            System.out.println("\n\n ============ Sports Queue ==========");
            System.out.println(consumerTag);
            System.out.println("SportsQ: " + new String(message.getBody()));
            System.out.println(message.getEnvelope());
        }), consumerTag -> {
            System.out.println(consumerTag);
        });
        channel.basicConsume("EducationQ", true, ((consumerTag, message) -> {
            System.out.println("\n\n ============ Education Queue ==========");
            System.out.println(consumerTag);
            System.out.println("EducationQ: " + new String(message.getBody()));
            System.out.println(message.getEnvelope());
        }), consumerTag -> {
            System.out.println(consumerTag);
        });
    }

    public static void publishMessage() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        String message = "Drink a lot of Water and stay Healthy!";
        channel.basicPublish("my-topic-exchange", "health.education", null, message.getBytes());
        message = "Learn something new everyday";
        channel.basicPublish("my-topic-exchange", "education", null, message.getBytes());
        message = "Stay fit in Mind and Body";
        channel.basicPublish("my-topic-exchange", "education.health", null, message.getBytes());
        channel.close();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        TopicExchange.declareExchange();
        TopicExchange.declareQueues();
        TopicExchange.declareBindings();
        Thread subscribe = new Thread(() -> {
            try {
                TopicExchange.subscribeMessage();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        });
        Thread publish = new Thread(() -> {
            try {
                TopicExchange.publishMessage();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        });
        subscribe.start();
        publish.start();
    }
}
