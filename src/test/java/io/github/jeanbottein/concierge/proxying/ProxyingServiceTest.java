package io.github.jeanbottein.concierge.proxying;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ProxyingServiceTest {
    private MockWebServer server;
    private ProxyingService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        service = new ProxyingService(WebClient.builder().build());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void get() throws InterruptedException {
        // given
        server.enqueue(new MockResponse().setBody("{}"));
        var request = new Proxy("/test", HttpMethod.GET, null, server.url("/").toString());

        // when
        service.proxy(request).block();

        // then
        var recorded = server.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("GET");
        assertThat(recorded.getPath()).isEqualTo("/test");
    }

    @Test
    void post() throws InterruptedException {
        // given
        server.enqueue(new MockResponse().setBody("{}"));
        var request = new Proxy("/test", HttpMethod.POST, "body", server.url("/").toString());

        // when
        service.proxy(request).block();

        // then
        var recorded = server.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getBody().readUtf8()).isEqualTo("body");
    }

    @Test
    void error() {
        // given
        server.enqueue(new MockResponse().setResponseCode(500));
        var request = new Proxy("/test", HttpMethod.GET, null, server.url("/").toString());

        // when & then
        StepVerifier.create(service.proxy(request))
            .expectError()
            .verify();
    }

} 