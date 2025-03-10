package io.github.jeanbottein.concierge.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "concierge")
public class ConciergeProperties {
    private Map<String, ServiceConfig> services;

    @Data
    public static class ServiceConfig {
        private String target;
    }
} 