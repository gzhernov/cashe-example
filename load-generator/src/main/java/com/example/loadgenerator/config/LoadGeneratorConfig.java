package com.example.loadgenerator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "load.generator")
public class LoadGeneratorConfig {
    private String targetUrl = "http://localhost:8080";
    private int threads = 1;
    private int requestsPerSecond = 10;
    private int minClientId = 1;
    private int maxClientId = 100;
    private boolean enabled = true;
    private long runDurationMinutes = 1; // 0 = бесконечно
}