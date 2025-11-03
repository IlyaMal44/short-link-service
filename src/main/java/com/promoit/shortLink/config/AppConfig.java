package com.promoit.shortLink.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Data
@Configuration
@EnableScheduling
public class AppConfig {
    @Value("${app.link.base-url:http://localhost:8080}")
    private String baseUrl;
    @Value("${app.link.default-ttl-hours:24}")
    private int defaultTtlHours;
    @Value("${app.link.code-length:9}")
    private int codeLength;
    @Value("${app.cleanup.interval:3600000}")  // 1 hour
    private String cleanupInterval;
}