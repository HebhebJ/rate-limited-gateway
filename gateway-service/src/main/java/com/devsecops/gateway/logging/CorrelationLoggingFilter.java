package com.devsecops.gateway.logging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class CorrelationLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationLoggingFilter.class);

    private static final String CORRELATION_ID = "X-Correlation-Id";
    private static final String API_KEY = "X-API-Key";

    @Override
    public int getOrder() {
        return -50; // after api-key auth, before most filters
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();

        String incomingCid = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID);
        final String correlationId = (incomingCid == null || incomingCid.isBlank())
                ? UUID.randomUUID().toString()
                : incomingCid;

        // Store for fallback + other filters/controllers
        exchange.getAttributes().put("correlationId", correlationId);

        // Forward to downstream
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.headers(h -> h.set(CORRELATION_ID, correlationId)))
                .build();

        // Return to client too
        mutatedExchange.getResponse().getHeaders().set(CORRELATION_ID, correlationId);

        String method = mutatedExchange.getRequest().getMethod() != null
                ? mutatedExchange.getRequest().getMethod().name()
                : "UNKNOWN";
        String path = mutatedExchange.getRequest().getURI().getPath();
        String apiKey = mutatedExchange.getRequest().getHeaders().getFirst(API_KEY);

        return chain.filter(mutatedExchange)
                .doFinally(signalType -> {
                    HttpStatusCode sc = mutatedExchange.getResponse().getStatusCode();
                    int status = (sc != null) ? sc.value() : 200;
                    long ms = System.currentTimeMillis() - start;

                    log.info("corrId={} status={} ms={} method={} path={} apiKey={} signal={}",
                            correlationId, status, ms, method, path, mask(apiKey), signalType);
                });
    }

    private String mask(String apiKey) {
        if (apiKey == null || apiKey.isBlank())
            return "none";
        if (apiKey.length() <= 4)
            return "****";
        return "****" + apiKey.substring(apiKey.length() - 4);
    }
}
