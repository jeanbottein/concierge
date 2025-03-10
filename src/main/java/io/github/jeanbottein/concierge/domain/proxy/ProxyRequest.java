package io.github.jeanbottein.concierge.domain.proxy;

import lombok.Value;
import org.springframework.http.HttpMethod;

@Value
public class ProxyRequest {
    String path;
    HttpMethod method;
    String body;
    String targetUrl;
} 