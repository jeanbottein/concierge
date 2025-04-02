# Concierge Camel

A Spring Boot application that provides reverse proxy functionality using Apache Camel.

## Features

- HTTP Reverse Proxy with dynamic route configuration
- Support for multiple backend services
- Configurable caching and resilience patterns

## Configuration

The proxy is configured through `application.yml`:

```yaml
concierge:
  proxies:
    serviceA:
      enabled: true
      target: "https://jsonplaceholder.typicode.com"
      caching:
        enabled: true
        ttl: 10s
    serviceB:
      enabled: true
      target: "https://jsonplaceholder.typicode.com"
```

## Running the Application

```bash
./gradlew bootRun
```

Or use the provided test script:

```bash
./test-proxy.sh
```

## Testing

Integration tests are included to verify that the proxy correctly forwards requests to backend services.

Run the tests:

```bash
./gradlew test
```

### Test Implementation

The integration test `ProxyIntegrationTest` directly compares responses from:
1. A direct call to the JSONPlaceholder API
2. A call through the proxy to the same endpoint

This verifies that the proxy is correctly forwarding requests and returning unmodified responses from the backend service.
