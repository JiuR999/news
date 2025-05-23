package com.swust.app.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class GpDefineFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("前置全局过滤器");
        return chain.filter(exchange).then(Mono.fromRunnable(()->{
            log.info("后置全局过滤器");
        }));
    }
    @Override
    public int getOrder() {
        return -2;
    }
}