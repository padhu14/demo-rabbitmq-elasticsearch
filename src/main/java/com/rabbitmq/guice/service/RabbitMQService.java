/*
 * Copyright (C) ThermoFisher Scientific Inc.- All Rights Reserved
 * Unauthorized use or copying of this file, via any medium is strictly prohibited and will be subject to legal action.
 * Proprietary and confidential
 *
 */
package com.rabbitmq.guice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.guice.constants.RabbitMQConstants;
import com.rabbitmq.guice.dto.Document;
import com.rabbitmq.guice.dto.Order;
import com.rabbitmq.guice.dto.OrderStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RabbitMQService {

    private static final List<OrderStatus> orderStatuses = new ArrayList<>();

    @Inject
    private Channel channel;

    @Inject
    private ObjectMapper objectMapper;

    public void getQueueResponse() throws IOException {
        AMQP.Queue.DeclareOk response = channel.queueDeclarePassive("queue-name");
// returns the number of messages in Ready state in the queue
        response.getMessageCount();
// returns the number of consumers the queue has
        response.getConsumerCount();
    }

    public void deleteQueue() throws IOException {
//        A queue or exchange can be explicitly deleted:

        channel.queueDelete("queue-name");
//        It is possible to delete a queue only if it is empty:

        channel.queueDelete("queue-name", false, true);
//        or if it is not used (does not have any consumers):

        channel.queueDelete("queue-name", true, false);
//        A queue can be purged (all of its messages deleted):

        channel.queuePurge("queue-name");
    }

    public void publishMessages(Document document, String restaurantName) throws IOException {
        if (!document.isDelete()) {
            Order order = objectMapper.readValue(objectMapper.writeValueAsBytes(document.getObject()), Order.class);
            order.setOrderId(UUID.randomUUID().toString());
            OrderStatus orderStatus = new OrderStatus(order, "PROCESS", "order placed succesfully in " + restaurantName);
            document.setObject(orderStatus);
        }
        byte[] messageBodyBytes = objectMapper.writeValueAsBytes(document);


        // Publish message
        channel.basicPublish(RabbitMQConstants.EXCHANGE, RabbitMQConstants.ROUTING_KEY,
                new AMQP.BasicProperties.Builder()
                        .contentType("application/json")
                        .build(),
                messageBodyBytes);
    }

    public List<OrderStatus> consumeMessage() throws IOException {

        GetResponse response = channel.basicGet(RabbitMQConstants.QUEUE, false);
        if (response != null) {
            AMQP.BasicProperties props = response.getProps();
            byte[] body = response.getBody();
            Document document = objectMapper.readValue(body, Document.class);
            orderStatuses.add(objectMapper.readValue(objectMapper.writeValueAsBytes(document.getObject()), OrderStatus.class));
            long deliveryTag = response.getEnvelope().getDeliveryTag();
            channel.basicAck(deliveryTag, true);
            if (response.getMessageCount() > 0) {
                consumeMessage();
            }
        }

        return orderStatuses;
    }


}
