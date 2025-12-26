package com.devsecops.gateway.errors;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Configuration
@Order(-2)
public class RateLimitErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // If it's already committed, bail out
        if (exchange.getResponse().isCommitted())
            return Mono.error(ex);

        // We only customize the 429 produced by gateway rate limiter
        if (exchange.getResponse().getStatusCode() != HttpStatus.TOO_MANY_REQUESTS) {
            return Mono.error(ex);
        }

        var response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {"error":"too_many_requests","message":"Rate limit exceeded. Please retry later."}
                """;

        var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
