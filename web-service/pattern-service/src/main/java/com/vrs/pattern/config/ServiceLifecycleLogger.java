package com.vrs.pattern.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceLifecycleLogger {

    @EventListener
    public void onReady(ApplicationReadyEvent event) {
        log.info("[SERVICE-LIFECYCLE] pattern-service started");
    }

    @EventListener
    public void onShutdown(ContextClosedEvent event) {
        log.info("[SERVICE-LIFECYCLE] pattern-service stopping");
    }
}
