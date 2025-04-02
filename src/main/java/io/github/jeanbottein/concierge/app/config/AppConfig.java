package io.github.jeanbottein.concierge.app.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import lombok.Data;

import static java.util.Collections.emptyList;

@Data
@Component
@ConfigurationProperties(prefix = "concierge")
public class AppConfig {
    
    private Map<String, ProxyConfig> proxies = new HashMap<>();
    
    @Data
    public static class ProxyConfig {
        private boolean enabled = true;
        private String target;
        
        @NestedConfigurationProperty
        private CachingConfig caching = new CachingConfig();
        
        @NestedConfigurationProperty
        private ResilienceConfig resilience = new ResilienceConfig();
    }
    
    @Data
    public static class CachingConfig {
        private boolean enabled = false;
        private Duration ttl = Duration.ofSeconds(10);
        private List<String> keyFields = emptyList();
    }
    
    @Data
    public static class ResilienceConfig {
        private boolean enabled = false;
        private int maxAttempts = 5;
        private Duration delay = Duration.ofSeconds(1);
        private int delayFactor = 2;
    }

}
