package com.devsecops.gateway.security;

import java.nio.charset.StandardCharsets;

import com.devsecops.gateway.config.ApiKeyProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class ApiKeyAuthFilter implements GlobalFilter, Ordered {

    private final ApiKeyProperties props;

    public ApiKeyAuthFilter(ApiKeyProperties props) {
        this.props = props;
    }

    private static final String[] PUBLIC_PATH_PREFIXES = { "/actuator", "/fallback" };

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublicPath(path))
            return chain.filter(exchange);

        HttpHeaders headers = exchange.getRequest().getHeaders();
        String apiKey = headers.getFirst(props.getApiKeyHeader());

        if (apiKey == null || apiKey.isBlank()) {
            return unauthorized(exchange, "Missing API key");
        }

        if (props.getValidApiKeys() == null || !props.getValidApiKeys().contains(apiKey)) {
            return unauthorized(exchange, "Invalid API key");
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix))
                return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"error\":\"unauthorized\",\"message\":\"" + escapeJson(message) + "\"}";
        var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}