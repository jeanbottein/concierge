package io.github.jeanbottein.concierge.proxying;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class ProxyingEndpoint {
    private final ProxyingService proxyingService;
    private final ProxyingProperties properties;

    ProxyingEndpoint(ProxyingService proxyingService, ProxyingProperties properties) {
        this.proxyingService = proxyingService;
        this.properties = properties;
    }

    @RequestMapping("/proxy/**")
    public Mono<ResponseEntity<String>> proxy(
            ServerWebExchange exchange,
            @RequestBody(required = false) String body
    ) {
        var path = exchange.getRequest().getPath().value()
                         .replaceFirst("/proxy", "");
        var target = properties.services().get("serviceA").target();
        
        return proxyingService.proxy(new Proxy(
                path,
                exchange.getRequest().getMethod(),
                body,
                target
        )).map(ResponseEntity::ok);
    }
} 