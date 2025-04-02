package io.github.jeanbottein.concierge.mock;

import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Configures WireMock stubs specifically for the proxy tests.
 * This class extends the shared WireMockTestSupport and adds
 * stub configurations specific to the proxy tests.
 */
public class ProxyWireMockConfig extends WireMockTestSupport {
    
    private static final String MOCK_TODO_RESPONSE = 
            "{\"id\": 1, \"title\": \"Sample Todo\", \"completed\": false, \"userId\": 42}";
    
    private static final String MOCK_USER_RESPONSE = 
            "{\"id\": 42, \"name\": \"John Doe\", \"email\": \"john@example.com\"}";
    
    private static final String MOCK_SOAP_RESPONSE = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://example.org/webservice\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <web:GetProductResponse>\n" +
            "         <web:product>\n" +
            "            <web:id>12345</web:id>\n" +
            "            <web:name>Sample Product</web:name>\n" +
            "            <web:price>99.99</web:price>\n" +
            "            <web:category>Electronics</web:category>\n" +
            "         </web:product>\n" +
            "      </web:GetProductResponse>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";
    
    /**
     * Sets up all the stub mappings needed for proxy tests.
     * This should be called once, typically in a @BeforeAll method.
     */
    public static void setupProxyStubs() {
        // Make sure the server is started
        startWireMockServer();
        
        // Configure stub for the todos/1 endpoint
        registerStub(get(urlEqualTo("/todos/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MOCK_TODO_RESPONSE)));
        
        // Configure stub for a second endpoint to test in parallel
        registerStub(get(urlEqualTo("/users/42"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MOCK_USER_RESPONSE)));
        
        // Configure stub for SOAP web service endpoint with very lenient matching
        registerStub(post(urlEqualTo("/soap/products"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml; charset=utf-8")
                        .withBody(MOCK_SOAP_RESPONSE)));
        
        // Add a fallback stub to log any unmatched requests (helps with debugging)
        registerStub(WireMock.any(WireMock.anyUrl())
                .atPriority(10) // Lower priority than other stubs
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("No matching stub found")));
    }
}
