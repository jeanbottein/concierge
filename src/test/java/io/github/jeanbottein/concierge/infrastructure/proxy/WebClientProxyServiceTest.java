package io.github.jeanbottein.concierge.infrastructure.proxy;

import io.github.jeanbottein.concierge.domain.proxy.ProxyRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientProxyServiceTest {

    private MockWebServer mockWebServer;
    private WebClientProxyService proxyService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
                
        proxyService = new WebClientProxyService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldProxyGetRequestSuccessfully() throws InterruptedException {
        // Given
        String expectedResponse = """
                {
                    "userId": 1,
                    "id": 1,
                    "title": "delectus aut autem",
                    "completed": false
                }""";
        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .addHeader("Content-Type", "application/json"));

        ProxyRequest request = new ProxyRequest("/todos/1", HttpMethod.GET, null,"http://jsonplaceholder.typicode.com"  );

        // When & Then
        StepVerifier.create(proxyService.proxyRequest(request))
                .expectNext(expectedResponse)
                .verifyComplete();

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/todos/1");
    }
} 