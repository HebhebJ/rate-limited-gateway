package com.devsecops.demo;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("/hello")
  public Map<String, Object> hello() {
    return Map.of(
        "service", "demo-service",
        "message", "hello through gateway",
        "time", Instant.now().toString());
  }
}
