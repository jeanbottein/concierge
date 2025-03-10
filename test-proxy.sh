#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "Building service..."
./gradlew build -x test -q > /dev/null 2>&1

echo "Starting service in background..."
java -jar build/libs/concierge-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &
SERVICE_PID=$!

# Wait for service to start
echo "Waiting for service to start..."
sleep 5

# Make requests
echo -e "\n${GREEN}Testing direct call to JSONPlaceholder:${NC}"
curl -s https://jsonplaceholder.typicode.com/todos/1 | jq .

echo -e "\n${GREEN}Testing proxied call through Concierge:${NC}"
curl -s http://localhost:8081/proxy/serviceA/todos/1 | jq .

# Kill the service
echo -e "\nStopping service..."
kill $SERVICE_PID > /dev/null 2>&1 