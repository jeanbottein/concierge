package io.github.jeanbottein.concierge.proxying;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ProxyingEndpoint {
    private static final Logger log = LoggerFactory.getLogger(ProxyingEndpoint.class);
    private final ProxyingService proxyingService;
    private final ProxyingProperties properties;

    ProxyingEndpoint(ProxyingService proxyingService, ProxyingProperties properties) {
        this.proxyingService = proxyingService;
        this.properties = properties;
    }

    @RequestMapping("/proxy/**")
    public Mono<String> proxy(HttpServletRequest request, @RequestBody(required = false) String body) {
        String fullPath = request.getRequestURI();
        log.debug("Received request: {} {}", request.getMethod(), fullPath);
        
        // Extract service name from path
        String path = fullPath.replaceFirst(".*/proxy/", "");
        String[] pathParts = path.split("/", 2);
        
        if (pathParts.length < 1) {
            log.error("Invalid path format: {}", path);
            return Mono.error(new IllegalArgumentException("Invalid path format"));
        }
        
        String serviceName = pathParts[0];
        String remainingPath = pathParts.length > 1 ? "/" + pathParts[1] : "/";
        
        log.debug("Service: {}, Path: {}", serviceName, remainingPath);
        
        // Check if service exists
        var serviceConfig = properties.services().get(serviceName);
        if (serviceConfig == null) {
            log.error("Service not found: {}", serviceName);
            return Mono.error(new IllegalArgumentException("Service not found: " + serviceName));
        }
        
        log.debug("Target URL: {}", serviceConfig.target());
        
        return proxyingService.proxy(new Proxy(
                remainingPath,
                HttpMethod.valueOf(request.getMethod()),
                body,
                serviceConfig.target()
        ))
        .doOnSuccess(response -> log.debug("Response received for {}", fullPath))
        .doOnError(error -> log.error("Error processing request: {}", error.getMessage()));
    }
} 