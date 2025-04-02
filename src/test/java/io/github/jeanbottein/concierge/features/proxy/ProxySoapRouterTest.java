package io.github.jeanbottein.concierge.features.proxy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import io.github.jeanbottein.concierge.mock.ProxyWireMockConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for the SOAP proxy functionality.
 * This test uses a local WireMock server to simulate the external SOAP service responses,
 * ensuring the proxy functionality can be tested without external dependencies.
 * 
 * The WireMock server is set up once before all tests and torn down after all tests,
 * which provides better performance when running multiple test methods in this class.
 * 
 * This test is configured to support parallel execution by avoiding request verification
 * and using response validation instead.
 * 
 * This test uses the shared ProxyWireMockConfig for WireMock server setup.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Execution(ExecutionMode.CONCURRENT)
class ProxySoapRouterTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @BeforeAll
    static void setupServer() {
        // Just configure stubs without starting/stopping the server
        // The server is already started by ProxyRouterTest
        try {
            // Use the shared WireMock configuration to set up stubs
            // This will reuse the existing server if already running
            ProxyWireMockConfig.setupProxyStubs();
        } catch (Exception e) {
            // If the server doesn't exist yet, log it but don't fail
            // ProxyRouterTest might start it
            System.out.println("Note: WireMock server not ready yet: " + e.getMessage());
        }
    }
    
    // No tearDown needed - ProxyRouterTest will handle server shutdown
    
    @Test
    @DisplayName("Should handle SOAP request/response through proxy from WireMock server")
    public void shouldProxySoapRequestCorrectly() throws Exception {
        // Given: A simplified SOAP request payload
        String soapRequest = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://example.org/webservice\">" +
                "<soapenv:Body>" +
                "<web:GetProductRequest>" +
                "<web:productId>12345</web:productId>" +
                "</web:GetProductRequest>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";
        
        // And: Headers for SOAP request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.add("SOAPAction", "http://example.org/webservice/GetProduct");
        
        HttpEntity<String> requestEntity = new HttpEntity<>(soapRequest, headers);
        
        // When: We make a SOAP call through our proxy
        ResponseEntity<String> proxyResponse = restTemplate.exchange(
                "/proxy/serviceA/soap/products",
                HttpMethod.POST,
                requestEntity,
                String.class);
        
        // Then: The response should be successful
        assertEquals(HttpStatus.OK, proxyResponse.getStatusCode(), "SOAP proxy call should return 200 OK");
        
        // And: The response should not be null
        String responseBody = proxyResponse.getBody();
        assertNotNull(responseBody, "SOAP proxy response body should not be null");
        
        // Basic validation of SOAP response content
        assertTrue(responseBody.contains("Envelope"), "Response should contain SOAP Envelope");
        assertTrue(responseBody.contains("GetProductResponse"), "Response should contain the expected response element");
        assertTrue(responseBody.contains("Sample Product"), "Response should contain the product name");
    }
}
