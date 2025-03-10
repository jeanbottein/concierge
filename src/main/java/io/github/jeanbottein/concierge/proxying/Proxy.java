package io.github.jeanbottein.concierge.proxying;

import org.springframework.http.HttpMethod;

record Proxy(String path, 
            HttpMethod method, 
            String body, 
            String targetUrl) {
    
    Proxy {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
    }
} 