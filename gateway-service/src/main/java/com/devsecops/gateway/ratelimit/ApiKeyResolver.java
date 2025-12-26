package com.devsecops.gateway.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component("apiKeyResolver")
public class ApiKeyResolver implements KeyResolver {

    private static final String HEADER = "X-API-Key";

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        String key = exchange.getRequest().getHeaders().getFirst(HEADER);
        return Mono.just(key != null ? key : "anonymous");
    }
}
