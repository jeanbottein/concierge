package io.github.jeanbottein.concierge.infrastructure.proxy;

import io.github.jeanbottein.concierge.application.ports.ProxyService;
import io.github.jeanbottein.concierge.domain.proxy.ProxyRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientProxyService implements ProxyService {
    private final WebClient webClient;
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    public WebClientProxyService() {
        this(WebClient.builder()
                .baseUrl(BASE_URL)
                .build());
    }

    public WebClientProxyService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<String> proxyRequest(ProxyRequest request) {
        WebClient.RequestBodySpec requestSpec = webClient
                .method(request.getMethod())
                .uri(request.getPath());

        if (request.getBody() != null) {
            return requestSpec.bodyValue(request.getBody())
                    .retrieve()
                    .bodyToMono(String.class);
        }

        return requestSpec.retrieve()
                .bodyToMono(String.class);
    }
} 