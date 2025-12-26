package com.devsecops.gateway.fallback;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
public class FallbackController {

    @GetMapping("/fallback/demo")
    public Map<String, Object> demoFallback(ServerWebExchange exchange) {
        String correlationId = (String) exchange.getAttribute("correlationId");
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "UNKNOWN";

        var body = new LinkedHashMap<String, Object>();
        body.put("time", Instant.now().toString());
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("message", "Fallback response from gateway");
        body.put("error", "upstream_unavailable");
        body.put("service", "demo-service");
        body.put("method", method);
        body.put("path", path);
        body.put("correlationId", correlationId);

        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return body;
    }
}
