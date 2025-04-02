package io.github.jeanbottein.concierge.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Base support class for tests that need a WireMock server.
 * This sets up a single shared WireMock server instance that can be used across multiple tests.
 */
public class WireMockTestSupport {
    
    private static WireMockServer wireMockServer;
    private static int mockServerPort;
    
    /**
     * Starts the WireMock server if it's not already running.
     * This is designed to be called from a @BeforeAll method.
     * 
     * This method is thread-safe and can be called from multiple test classes concurrently.
     * If the server is already running, it will simply return without error.
     */
    public static synchronized void startWireMockServer() {
        try {
            if (wireMockServer == null || !wireMockServer.isRunning()) {
                // Use a dynamically allocated port to avoid conflicts
                // This should prevent port binding issues between test runs
                wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
                wireMockServer.start();
                
                // Get the dynamically allocated port
                mockServerPort = wireMockServer.port();
                System.out.println("Started WireMock server on dynamically allocated port: " + mockServerPort);
                
                // Store the port in system properties so any other code can access it
                System.setProperty("mockServerPort", String.valueOf(mockServerPort));
            } else {
                System.out.println("WireMock server already running on port: " + mockServerPort);
            }
        } catch (Exception e) {
            System.err.println("Error starting WireMock server: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow the exception as this is a critical test setup issue
        }
    }
    
    /**
     * Stops the WireMock server if it's running.
     * This is designed to be called from an @AfterAll method.
     * 
     * This method is thread-safe and can be called from multiple test classes concurrently.
     * Multiple calls to stop the server are safe and will not cause errors.
     */
    public static synchronized void stopWireMockServer() {
        try {
            if (wireMockServer != null && wireMockServer.isRunning()) {
                System.out.println("Stopping WireMock server on port: " + mockServerPort);
                wireMockServer.stop();
                System.out.println("WireMock server stopped successfully");
            } else {
                System.out.println("WireMock server already stopped or not running");
            }
        } catch (Exception e) {
            System.err.println("Error stopping WireMock server: " + e.getMessage());
            // Log but don't throw - we don't want test teardown to fail
        }
    }
    
    /**
     * Returns the running WireMock server instance.
     * If the server is not running, it will attempt to start it first.
     * 
     * @return The running WireMock server instance
     */
    public static WireMockServer getWireMockServer() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            System.out.println("WireMock server not running, attempting to start it");
            startWireMockServer();
            
            // Double-check that it started successfully
            if (wireMockServer == null || !wireMockServer.isRunning()) {
                throw new IllegalStateException("Failed to start WireMock server automatically");
            }
        }
        return wireMockServer;
    }
    
    /**
     * Registers a stub with the WireMock server.
     * This provides a convenience method to add stubs to the shared server.
     * If the server is not running, it will attempt to start it first.
     * 
     * @param mappingBuilder The mapping to register with WireMock
     */
    public static void registerStub(MappingBuilder mappingBuilder) {
        try {
            getWireMockServer().stubFor(mappingBuilder);
        } catch (Exception e) {
            System.err.println("Error registering WireMock stub: " + e.getMessage());
            throw e; // Rethrow as this is likely a serious test setup issue
        }
    }
    
    /**
     * Gets the port that the WireMock server is running on.
     */
    public static int getMockServerPort() {
        return mockServerPort;
    }
    
    /**
     * Utility method for setting up Spring properties to use the mock server.
     * This is particularly useful when you need to set the mock server URL dynamically.
     */
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mock.server.port", () -> mockServerPort);
        registry.add("mock.server.url", () -> "http://localhost:" + mockServerPort);
    }
}
