package io.github.jeanbottein.concierge.proxying;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("proxying")
record ProxyingProperties(Map<String, ServiceProperties> services) {
    record ServiceProperties(String target) {}
} 