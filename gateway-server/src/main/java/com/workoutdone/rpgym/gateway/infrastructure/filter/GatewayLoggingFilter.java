package com.workoutdone.rpgym.gateway.infrastructure.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class GatewayLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String traceId = UUID.randomUUID().toString();
        long startTime = System.nanoTime();

        MDC.put("traceId", traceId);

        log.info(
                "[{}] Gateway request: {} {}",
                traceId,
                method,
                path
        );

        return chain.filter(exchange)
                .doFinally(signal -> {
                    long latency = (System.nanoTime() - startTime) / 1_000_000;

                    log.info(
                            "[{}] Gateway response: {} {} {}ms {}",
                            traceId,
                            method,
                            path,
                            latency,
                            exchange.getResponse().getStatusCode()
                    );

                    MDC.remove("traceId");
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}