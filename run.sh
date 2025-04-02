#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Build and run the application with debug logging enabled

echo "Building application..."
./gradlew build -x test

echo "Running application with debug logging..."
java -Dlogging.level.io.github.jeanbottein=DEBUG -jar build/libs/concierge-0.0.1-SNAPSHOT.jar
