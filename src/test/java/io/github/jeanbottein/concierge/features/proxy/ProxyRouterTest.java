package io.github.jeanbottein.concierge.features.proxy;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import io.github.jeanbottein.concierge.mock.ProxyWireMockConfig;
import io.github.jeanbottein.concierge.mock.WireMockTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for the REST proxy functionality.
 * This test uses a local WireMock server to simulate the external REST API responses,
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
class ProxyRouterTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    // Set a high JUnit test execution order to ensure this class runs first
    // and starts the WireMock server before other test classes
    @BeforeAll
    static void setupServer() {
        try {
            // Use the shared WireMock configuration to set up all the needed stubs
            // This will start the server if not already running
            ProxyWireMockConfig.setupProxyStubs();
            System.out.println("ProxyRouterTest: WireMock server setup complete");
        } catch (Exception e) {
            System.err.println("ProxyRouterTest: Error setting up WireMock server: " + e.getMessage());
            // If it fails due to address binding, it might be that another process is using the port
            if (e.getMessage() != null && e.getMessage().contains("Address already in use")) {
                // We'll try to continue with tests - the port might be in use by a previous test run
                // that didn't shut down properly
                System.out.println("ProxyRouterTest: Port already in use, trying to continue tests");
            } else {
                throw e; // Rethrow other exceptions
            }
        }
    }
    
    // This should be the last test class to tear down the WireMock server
    @AfterAll
    static void tearDown() {
        try {
            // Only stop the WireMock server when all tests are done
            // This class should be the last to execute the tearDown method
            System.out.println("ProxyRouterTest: Stopping WireMock server");
            WireMockTestSupport.stopWireMockServer();
        } catch (Exception e) {
            System.err.println("ProxyRouterTest: Error stopping WireMock server: " + e.getMessage());
            // Log but don't fail tests
        }
    }
    

    @Test
    @DisplayName("Should receive todo data through proxy from WireMock server")
    public void shouldProxyRequestCorrectly() {
        // The shared WireMock server should be running from our @BeforeAll
        
        // When: We make a call through our proxy
        ResponseEntity<String> proxyResponse = restTemplate.getForEntity(
                "/proxy/serviceA/todos/1", 
                String.class);
        
        // Then: The response should be successful
        assertEquals(HttpStatus.OK, proxyResponse.getStatusCode(), "Proxy call should return 200 OK");
        
        // And: The response should not be null
        assertNotNull(proxyResponse.getBody(), "Proxy response body should not be null");
        
        // For parallel tests, we skip request verification and focus on response validation
        // wireMockServer.verify(WireMock.getRequestedFor(urlEqualTo("/todos/1")));
        
        // And: The response should contain JSON with expected fields
        String responseBody = proxyResponse.getBody();
        assertNotNull(responseBody);
        
        // Verify the response contains expected structure
        assertTrue(responseBody.contains("\"id\":"), "Response should contain an ID field");
        assertTrue(responseBody.contains("\"title\":"), "Response should contain a title field");
        assertTrue(responseBody.contains("\"completed\":"), "Response should contain a completed field");
        assertTrue(responseBody.contains("\"userId\":"), "Response should contain a userId field");
    }
    
    @Test
    @DisplayName("Should receive user data through proxy from WireMock server")
    public void shouldProxyUserRequestCorrectly() {
        // When: We make a call through our proxy to the users endpoint
        ResponseEntity<String> proxyResponse = restTemplate.getForEntity(
                "/proxy/serviceA/users/42", 
                String.class);
        
        // Then: The response should be successful
        assertEquals(HttpStatus.OK, proxyResponse.getStatusCode(), "Proxy call should return 200 OK");
        
        // And: The response should not be null
        assertNotNull(proxyResponse.getBody(), "Proxy response body should not be null");
        
        // For parallel tests, we skip request verification and focus on response validation
        
        // And: The response should contain JSON with expected fields
        String responseBody = proxyResponse.getBody();
        assertNotNull(responseBody);
        
        // Verify the response contains expected structure
        assertTrue(responseBody.contains("\"id\":"), "Response should contain an ID field");
        assertTrue(responseBody.contains("\"name\":"), "Response should contain a name field");
        assertTrue(responseBody.contains("\"email\":"), "Response should contain an email field");
    }
    

}
