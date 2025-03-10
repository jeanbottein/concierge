package io.github.jeanbottein.concierge.proxying;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class ProxyingService {
    private final WebClient webClient;
    
    ProxyingService(WebClient webClient) {
        this.webClient = webClient;
    }
    
    Mono<String> proxy(Proxy proxy) {
        var requestSpec = webClient.method(proxy.method())
                .uri(proxy.targetUrl() + proxy.path());
                
        if (proxy.body() != null) {
            return requestSpec.bodyValue(proxy.body())
                    .retrieve()
                    .bodyToMono(String.class);
        }
        
        return requestSpec.retrieve()
                .bodyToMono(String.class);
    }
} 