package com.rabbitmq.guice;

import com.google.common.collect.Lists;
import com.thermofisher.cloud.platform.microservice.ServiceModule;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import javax.ws.rs.client.ClientRequestFilter;

public abstract class DECPServiceModule extends ServiceModule {
    protected <T> T client(Class<T> apiClass, String address, ClientRequestFilter linkerdFilter, Object... providers) {
        return JAXRSClientFactory.create(address, apiClass, Lists.asList(linkerdFilter, providers));
    }
}