package io.github.jeanbottein.concierge.proxying;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Map;

@WebFluxTest(ProxyingEndpoint.class)
@Import(ProxyingEndpointTests.TestConfig.class)
class ProxyingEndpointTests {
    
    @Autowired
    private WebTestClient client;
    
    @Autowired
    private ProxyingService service;
    
    @TestConfiguration
    static class TestConfig {
        @Bean
        ProxyingService proxyingService() {
            return mock(ProxyingService.class);
        }
        
        @Bean
        ProxyingProperties proxyingProperties() {
            return new ProxyingProperties(Map.of(
                "serviceA", new ProxyingProperties.ServiceProperties("https://api.example.com")
            ));
        }
    }
    
    @Test
    void shouldProxyGetRequest() {
        var response = """
            {"id": 1, "title": "test"}
            """;
            
        when(service.proxy(eq(new Proxy(
            "/todos/1",
            HttpMethod.GET,
            null,
            "https://api.example.com"
        )))).thenReturn(Mono.just(response));
        
        client.get()
            .uri("/proxy/todos/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .isEqualTo(response);
    }

    @Test
    void shouldProxyPostRequest() {
        var requestBody = """
            {"title": "new test"}
            """;
        var response = """
            {"id": 201, "title": "new test"}
            """;
            
        when(service.proxy(eq(new Proxy(
            "/todos",
            HttpMethod.POST,
            requestBody,
            "https://api.example.com"
        )))).thenReturn(Mono.just(response));
        
        client.post()
            .uri("/proxy/todos")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .isEqualTo(response);
    }

    @Test
    void shouldHandleServiceError() {
        when(service.proxy(any(Proxy.class)))
            .thenReturn(Mono.error(new RuntimeException("Service unavailable")));
        
        client.get()
            .uri("/proxy/todos/1")
            .exchange()
            .expectStatus().is5xxServerError();
    }

    @Test
    void shouldHandleInvalidService() {
        client.get()
            .uri("/proxy/invalidService/todos")
            .exchange()
            .expectStatus().isNotFound();
    }
} 