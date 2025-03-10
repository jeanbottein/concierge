package io.github.jeanbottein.concierge.proxying;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class ProxyingConfiguration {
    
    @Bean
    WebClient webClient() {
        return WebClient.builder().build();
    }
    
    @Bean
    ProxyingService proxyingService(WebClient webClient) {
        return new ProxyingService(webClient);
    }
} 