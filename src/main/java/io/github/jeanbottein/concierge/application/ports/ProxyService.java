package io.github.jeanbottein.concierge.application.ports;

import io.github.jeanbottein.concierge.domain.proxy.ProxyRequest;
import reactor.core.publisher.Mono;

public interface ProxyService {
    Mono<String> proxyRequest(ProxyRequest request);
} 