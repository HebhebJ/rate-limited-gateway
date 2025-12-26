package com.devsecops.gateway.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class ApiKeyProperties {

    private String apiKeyHeader = "X-API-Key";
    private List<String> validApiKeys = new ArrayList<>();

    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    public void setApiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
    }

    public List<String> getValidApiKeys() {
        return validApiKeys;
    }

    public void setValidApiKeys(List<String> validApiKeys) {
        this.validApiKeys = validApiKeys;
    }
}