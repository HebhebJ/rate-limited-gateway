package com.devsecops.demo;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("/hello")
  public Map<String, Object> hello(@RequestHeader(value = "X-Correlation-Id", required = false) String cid) {
    return Map.of(
        "time", Instant.now().toString(),
        "message", "hello through gateway",
        "service", "demo-service",
        "correlationId", cid);
  }

}
