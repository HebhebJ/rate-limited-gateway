package com.devsecops.gateway.ratelimit;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component("apiKeyOrIpResolver")
public class ApiKeyOrIpKeyResolver implements KeyResolver {

    @Value("${app.security.api-key-header:X-API-Key}")
    private String apiKeyHeader;

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        String apiKey = exchange.getRequest().getHeaders().getFirst(apiKeyHeader);
        if (apiKey != null && !apiKey.isBlank()) {
            // namespace keys so redis buckets don’t collide with IP ones
            return Mono.just("key:" + apiKey);
        }

        // If behind proxy, consider X-Forwarded-For (see note below)
        String ip = extractClientIp(exchange);
        return Mono.just("ip:" + ip);
    }

    private String extractClientIp(ServerWebExchange exchange) {
        // 1) Try X-Forwarded-For first (first IP is original client)
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }

        // 2) Fallback: remote address
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        if (remote != null && remote.getAddress() != null) {
            return remote.getAddress().getHostAddress();
        }

        return "unknown";
    }
}
