/*
 * Copyright (C) ThermoFisher Scientific Inc.- All Rights Reserved
 * Unauthorized use or copying of this file, via any medium is strictly prohibited and will be subject to legal action.
 * Proprietary and confidential
 *
 */
package com.rabbitmq.guice.api;

import com.google.inject.Inject;
import com.rabbitmq.guice.dto.Document;
import com.rabbitmq.guice.dto.Order;
import com.rabbitmq.guice.dto.OrderStatus;
import com.rabbitmq.guice.service.RabbitMQService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Path("/rabbit")
public class RabbitMQAPI {

    @Inject
    private RabbitMQService rabbitMQService;


    @GET
    @Path("/consumer")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrderStatus> consumeMessages() {
        try {
            return rabbitMQService.consumeMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @POST
    @Path("/producer/{restaurantName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String produceMessage(Document document, @PathParam("restaurantName") String restaurantName) {

        try {
            rabbitMQService.publishMessages(document, restaurantName);
            return " Response - OK";
        } catch (IOException e) {
            return e.getMessage();
        }
    }


}
