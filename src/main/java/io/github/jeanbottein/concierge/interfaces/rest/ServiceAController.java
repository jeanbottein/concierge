package io.github.jeanbottein.concierge.interfaces.rest;

import io.github.jeanbottein.concierge.application.ports.ProxyService;
import io.github.jeanbottein.concierge.domain.proxy.ProxyRequest;
import io.github.jeanbottein.concierge.infrastructure.config.ConciergeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/${concierge.services[0]}/**")
@RequiredArgsConstructor
public class ServiceAController {

    private final ProxyService proxyService;
    private final ConciergeProperties properties;

    @RequestMapping
    public Mono<ResponseEntity<String>> proxyRequest(
            ServerWebExchange exchange,
            @RequestBody(required = false) String body
    ) {
        String serviceName = properties.getServices().get(0);
        String path = exchange.getRequest().getPath().value()
                            .replaceFirst("/" + serviceName, "");
        ProxyRequest proxyRequest = new ProxyRequest(
                path,
                exchange.getRequest().getMethod(),
                body
        );

        return proxyService.proxyRequest(proxyRequest)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
} 