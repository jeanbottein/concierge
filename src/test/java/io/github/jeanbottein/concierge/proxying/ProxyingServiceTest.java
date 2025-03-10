package io.github.jeanbottein.concierge.proxying;

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

class ProxyingServiceTests {
    private MockWebServer mockWebServer;
    private ProxyingService service;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
                
        service = new ProxyingService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldProxyGetRequestSuccessfully() throws InterruptedException {
        // Given
        var expectedResponse = """
                {
                    "userId": 1,
                    "id": 1,
                    "title": "test",
                    "completed": false
                }""";
        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .addHeader("Content-Type", "application/json"));

        var request = new Proxy(
            "/todos/1", 
            HttpMethod.GET, 
            null,
            mockWebServer.url("/").toString()
        );

        // When & Then
        StepVerifier.create(service.proxy(request))
                .expectNext(expectedResponse)
                .verifyComplete();

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/todos/1");
    }

    @Test
    void shouldProxyPostRequestWithBody() throws InterruptedException {
        // Given
        var requestBody = """
                {
                    "title": "new todo",
                    "completed": false
                }""";
        var expectedResponse = """
                {
                    "id": 201,
                    "title": "new todo",
                    "completed": false
                }""";
        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .addHeader("Content-Type", "application/json"));

        var request = new Proxy(
            "/todos", 
            HttpMethod.POST, 
            requestBody,
            mockWebServer.url("/").toString()
        );

        // When & Then
        StepVerifier.create(service.proxy(request))
                .expectNext(expectedResponse)
                .verifyComplete();

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/todos");
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void shouldHandleErrorResponse() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        var request = new Proxy(
            "/invalid", 
            HttpMethod.GET, 
            null,
            mockWebServer.url("/").toString()
        );

        // When & Then
        StepVerifier.create(service.proxy(request))
                .expectError()
                .verify();
    }
} 