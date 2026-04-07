package com.vrs.gateway.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ServiceLifecycleLogger {

    private static final Logger log = LoggerFactory.getLogger(ServiceLifecycleLogger.class);

    @EventListener
    public void onReady(ApplicationReadyEvent event) {
        log.info("[SERVICE-LIFECYCLE] api-gateway started");
    }

    @EventListener
    public void onShutdown(ContextClosedEvent event) {
        log.info("[SERVICE-LIFECYCLE] api-gateway stopping");
    }
}
