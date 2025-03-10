package io.github.jeanbottein.concierge.interfaces.rest;

import io.github.jeanbottein.concierge.application.ports.ProxyService;
import io.github.jeanbottein.concierge.domain.proxy.ProxyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(ServiceAController.class)
class ServiceAControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProxyService proxyService;

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