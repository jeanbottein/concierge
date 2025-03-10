package io.github.jeanbottein.concierge.interfaces.rest;

import io.github.jeanbottein.concierge.application.ports.ProxyService;
import io.github.jeanbottein.concierge.domain.proxy.ProxyRequest;
import io.github.jeanbottein.concierge.infrastructure.config.ConciergeProperties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.context.annotation.Import;

@WebFluxTest(ServiceAController.class)
@Import(ServiceAControllerTest.TestConfig.class)
class ServiceAControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProxyService proxyService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ConciergeProperties conciergeProperties() {
            ConciergeProperties properties = new ConciergeProperties();
            properties.setServices(List.of("serviceA"));
            return properties;
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
        when(proxyService.proxyRequest(any(ProxyRequest.class)))
                .thenReturn(Mono.just(expectedResponse));

        // When & Then
        webTestClient.get()
                .uri("/serviceA/todos/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(expectedResponse);

        // Verify the correct ProxyRequest was created
        verify(proxyService).proxyRequest(new ProxyRequest("/todos/1", HttpMethod.GET, null));
    }
} 