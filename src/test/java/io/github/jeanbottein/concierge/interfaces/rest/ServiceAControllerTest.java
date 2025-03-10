package io.github.jeanbottein.concierge.interfaces.rest;

import io.github.jeanbottein.concierge.application.ports.ProxyService;
import io.github.jeanbottein.concierge.domain.proxy.ProxyRequest;
import io.github.jeanbottein.concierge.infrastructure.config.ConciergeProperties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.eq;

import org.springframework.context.annotation.Import;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@WebFluxTest(ServiceAController.class)
@Import(ServiceAControllerTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
class ServiceAControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProxyService proxyService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ConciergeProperties conciergeProperties() {
            ConciergeProperties properties = new ConciergeProperties();
            Map<String, ConciergeProperties.ServiceConfig> services = new HashMap<>();
            
            ConciergeProperties.ServiceConfig serviceA = new ConciergeProperties.ServiceConfig();
            serviceA.setTarget("https://jsonplaceholder.typicode.com");
            services.put("serviceA", serviceA);
            
            properties.setServices(services);
            return properties;
        }

        @Bean
        public ProxyService proxyService() {
            return mock(ProxyService.class);
        }
    }

    @Test
    void shouldProxyGetRequestSuccessfully() {
        // Given
        String expectedResponse = """
                {
                    "userId": 1,
                    "id": 1,
                    "title": "delectus aut autem",
                    "completed": false
                }""";
        
        ProxyRequest expectedRequest = new ProxyRequest(
            "/todos/1", 
            HttpMethod.GET, 
            null, 
            "https://jsonplaceholder.typicode.com"
        );
        
        when(proxyService.proxyRequest(eq(expectedRequest)))
                .thenReturn(Mono.just(expectedResponse));

        // When & Then
        webTestClient.get()
                .uri("/serviceA/todos/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(expectedResponse);

        verify(proxyService).proxyRequest(expectedRequest);
    }
} 