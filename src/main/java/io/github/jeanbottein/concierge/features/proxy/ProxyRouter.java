package io.github.jeanbottein.concierge.features.proxy;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import io.github.jeanbottein.concierge.app.config.AppConfig;
import io.github.jeanbottein.concierge.app.config.AppConfig.ProxyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dynamic proxy route handler that creates routes for all proxies configured under concierge.proxies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AppConfig.class)
public class ProxyRouter extends RouteBuilder {

    private final AppConfig appConfig;
    
    private static final String PROXY_PATH_PREFIX = "/proxy/";

    @Override
    public void configure() throws Exception {
        log.info("Configuring proxy routes from: {}", appConfig.getProxies());
        
        if (appConfig.getProxies() == null || appConfig.getProxies().isEmpty()) {
            log.warn("No proxies configured! Check your configuration under concierge.proxies");
            return;
        }
        appConfig.getProxies().forEach(this::configureProxyRoute);
    }
    
    private void configureProxyRoute(String proxyName, ProxyConfig proxyConfig) {
        if (!proxyConfig.isEnabled()) {
            log.info("Skipping disabled proxy route for {}", proxyName);
            return;
        }

        var proxyBasePath = PROXY_PATH_PREFIX + proxyName;
        var targetUrl = proxyConfig.getTarget();
        
        log.info("Configuring proxy route for {}, target: {}", proxyName, targetUrl);
        
        // Create the route for this proxy
        from("platform-http:" + proxyBasePath + "?matchOnUriPrefix=true")
            .routeId("proxy-" + proxyName)
            .process(exchange -> extractPath(exchange, proxyBasePath, targetUrl))
            // Copy the HTTP method from CamelHttpMethod header
            .setHeader(Exchange.HTTP_METHOD, header("CamelHttpMethod"))
            // Set content type for JSON handling
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            // Set accept header for JSON responses
            .setHeader("Accept", constant("application/json"))
            // Remove Camel-specific headers except the ones we need
            .removeHeaders("Camel*", "CamelHttpMethod")
            .toD("${exchangeProperty.targetUrl}${exchangeProperty.remainingPath}?bridgeEndpoint=true&throwExceptionOnFailure=false")
            .process(this::logResponse);
    }
    

    /**
     * Logs response details including status code and target URL
     */
    private void logResponse(Exchange exchange) {
        var responseCode = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        // Using a modern switch expression for status description
        var status = switch(Integer.compare(responseCode, 200)) {
            case 0 -> "OK";
            default -> "ERROR";
        };
        
        log.info("[RESPONSE] {} {} (from: {}{})", 
            responseCode,
            status,
            exchange.getProperty("targetUrl"),
            exchange.getProperty("remainingPath"));
    }

    private void extractPath(Exchange exchange, String proxyBasePath, String targetUrl) {
        var requestUri = exchange.getIn().getHeader("CamelHttpUri", String.class);
        var httpMethod = exchange.getIn().getHeader("CamelHttpMethod", String.class);
        var rawQuery = exchange.getIn().getHeader("CamelHttpRawQuery", String.class);
        
        // Extract the remaining path
        var remainingPath = extractRemainingPath(requestUri, proxyBasePath);
        
        // Add query parameters if present - using string concatenation instead of StringBuilder for simple cases
        if (rawQuery != null && !rawQuery.isEmpty()) {
            remainingPath = remainingPath + "?" + rawQuery;
            log.info("[REQUEST] Query parameters: {} for path: {}", rawQuery, remainingPath.substring(0, remainingPath.indexOf('?')));
        }
        
        log.info("[PROXY] {} {} -> {}{}", httpMethod, requestUri, targetUrl, remainingPath);
        
        // Set properties for the outgoing exchange
        exchange.setProperty("remainingPath", remainingPath);
        exchange.setProperty("targetUrl", targetUrl);
        
        // Preserve important headers, removing those that might interfere
        preserveHeaders(exchange);
        
        // Log request headers for debugging - using pattern matching for filtering
        exchange.getIn().getHeaders().forEach((key, value) -> {
            if (key instanceof String k && (k.startsWith("Camel") || k.startsWith("Content") || k.startsWith("Accept"))) {
                log.debug("[HEADER] {} = {}", key, value);
            }
        });
    }
    
    /**
     * Preserves important headers for the proxied request.
     */
    private void preserveHeaders(Exchange exchange) {
        // Headers to remove (these will be set by Camel or the target service)
        // Using Array for header names
        var headersToRemove = new String[] {
            "CamelHttpUri", "CamelHttpUrl", 
            "Host", "Content-Length", "Connection", "Transfer-Encoding"
        };
        
        // Remove each header
        for (var header : headersToRemove) {
            exchange.getIn().removeHeader(header);
        }
        
        // Ensure these important headers are preserved or set
        // Using putIfAbsent pattern from modern Java
        var headers = exchange.getIn().getHeaders();
        if (!headers.containsKey("Accept")) {
            exchange.getIn().setHeader("Accept", "application/json");
        }
        
        // Add a custom header to identify the proxy
        exchange.getIn().setHeader("X-Forwarded-By", "Concierge-Proxy");
    }
    
    /**
     * Extracts the part of the path that follows the proxy base path.
     */
    private String extractRemainingPath(String requestUri, String proxyBasePath) {
        // Using Optional to handle null safely
        return Optional.ofNullable(requestUri)
            .filter(uri -> uri.startsWith(proxyBasePath))
            .map(uri -> {
                var path = uri.substring(proxyBasePath.length());
                return path.isEmpty() ? "/" : path;
            })
            .orElse("/");
    }
    
 
}