package io.github.jeanbottein.concierge.interfaces.rest;

import io.github.jeanbottein.concierge.application.ports.ProxyService;
import io.github.jeanbottein.concierge.domain.proxy.ProxyRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/serviceA/**")
@RequiredArgsConstructor
public class ServiceAController {

    private final ProxyService proxyService;

    @RequestMapping
    public Mono<ResponseEntity<String>> proxyRequest(
            HttpServletRequest request,
            @RequestBody(required = false) String body
    ) {
        String path = request.getRequestURI().replaceFirst("/serviceA", "");
        ProxyRequest proxyRequest = new ProxyRequest(
                path,
                HttpMethod.valueOf(request.getMethod()),
                body
        );

        return proxyService.proxyRequest(proxyRequest)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
} 