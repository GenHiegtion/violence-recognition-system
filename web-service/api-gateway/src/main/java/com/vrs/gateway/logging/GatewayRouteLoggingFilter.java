package com.vrs.gateway.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayRouteLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GatewayRouteLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String path = request.getURI().getPath();

        Route route = exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute");
        String routeId = route != null ? route.getId() : "unmatched";
        String target = route != null ? route.getUri().toString() : "n/a";

        long start = System.currentTimeMillis();
        log.info("[ROUTE-OUTBOUND] api-gateway forwarding {} {} route={} target={}", method, path, routeId, target);

        return chain.filter(exchange)
                .doOnSuccess((unused) -> {
                    long duration = System.currentTimeMillis() - start;
                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;
                    log.info(
                            "[ROUTE-OUTBOUND] api-gateway completed {} {} route={} status={} durationMs={}",
                            method,
                            path,
                            routeId,
                            status,
                            duration
                    );
                })
                .doOnError((error) -> {
                    long duration = System.currentTimeMillis() - start;
                    log.error(
                            "[ROUTE-OUTBOUND] api-gateway failed {} {} route={} durationMs={} error={}",
                            method,
                            path,
                            routeId,
                            duration,
                            error.getMessage()
                    );
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
