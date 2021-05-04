/*
 * Copyright (C) ThermoFisher Scientific Inc.- All Rights Reserved
 * Unauthorized use or copying of this file, via any medium is strictly prohibited and will be subject to legal action.
 * Proprietary and confidential
 *
 */
package com.rabbitmq.guice;

import com.google.common.collect.Sets;
import com.google.inject.Module;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.rabbitmq.client.*;
import com.rabbitmq.guice.api.RabbitMQAPI;
import com.rabbitmq.guice.constants.RabbitMQConstants;
import com.rabbitmq.guice.service.RabbitMQService;
import com.thermofisher.cloud.platform.microservice.GuiceApplication;
import com.thermofisher.cloud.platform.microservice.JettyLauncher;
import com.thermofisher.cloud.sdk.authkeyprovider.jaxrs.AuthKeyProviderRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Launcher extends JettyLauncher {


    public static void main(String[] args) {
        contextPath("/rabbit");
        servletPath("/api/*");
        application(new RabbitApplication());
        port(8080);
        Injector injector = Guice.createInjector(new RabbitMQInjector());
        serve();
    }


    private static class RabbitMQInjector extends DECPServiceModule {

//        private final ExecutorService sharedExecutor = Executors.newSingleThreadExecutor();

        @Override
        protected void configure() {
            bindRequiredName("LTAS_SERVICE_NAME", "LTAS_SERVICE_NAME");
            bindRequiredName("RABBITMQ_HOST", "RABBITMQ_HOST");
            bindRequiredName("RABBITMQ_PORT", "RABBITMQ_PORT");
            bindRequiredName("RABBITMQ_USERNAME", "RABBITMQ_USERNAME");
            bindRequiredName("RABBITMQ_PWD", "RABBITMQ_PWD");
            bindRequiredName("RABBITMQ_VIRTUALHOST", "RABBITMQ_VIRTUALHOST");

            bind(RabbitMQAPI.class);
            bind(RabbitMQService.class);

        }

        @Provides
        @Inject
        @Singleton
        public Connection getConnection(@Named("RABBITMQ_USERNAME") String userName, @Named("RABBITMQ_PWD") String password,
                                        @Named("RABBITMQ_VIRTUALHOST") String virtualHost, @Named("RABBITMQ_HOST") String hostName,
                                        @Named("RABBITMQ_PORT") int portNumber) throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostName);
            factory.setPort(portNumber);

            return factory.newConnection();
        }

        @Provides
        @Inject
        @Singleton
        public Channel openChannel(Connection connection) throws IOException {
            Channel channel = connection.createChannel();
            connection.addShutdownListener(cause -> {
                if (cause.isHardError()) {
                    Connection conn = (Connection) cause.getReference();
                    if (!cause.isInitiatedByApplication()) {
                        Method reason = cause.getReason();
                    }
                } else {
                    Channel ch = (Channel) cause.getReference();
                }
                stop(channel, connection);
            });

            // Binding
            channel.exchangeDeclare(RabbitMQConstants.EXCHANGE, BuiltinExchangeType.DIRECT, true);
            channel.queueDeclare(RabbitMQConstants.QUEUE, true, false, false, null);
            channel.queueBind(RabbitMQConstants.QUEUE, RabbitMQConstants.EXCHANGE, RabbitMQConstants.ROUTING_KEY);

            return channel;
        }

        public void stop(Channel channel, Connection connection) {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();

                }
            }
            if (connection != null && connection.isOpen()) {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private static class RabbitApplication extends GuiceApplication {

        @Override
        protected Set<Module> modules() {
            return Sets.newHashSet(new RabbitMQInjector());
        }

        @Override
        protected Set<Object> serviceInstances(Injector injector) {

            /**
             * Add your API classes here too
             */
            return Sets.newHashSet(
                    injector.getInstance(RabbitMQAPI.class));
        }

        @Override
        protected Set<Object> providers(Injector injector) {
            Set<Object> providerSet = Sets.newHashSet();

            providerSet.add(new AuthKeyProviderRequestFilter(injector.getInstance(Key.get(String.class, Names.named("LTAS_SERVICE_NAME")))));
            return providerSet;
        }
    }
}
