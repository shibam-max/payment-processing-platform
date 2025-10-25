#!/bin/bash

# Build script for Payment Processing Platform

set -e

echo "🚀 Building Payment Processing Platform..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

print_success "Prerequisites check passed"

# Clean and compile
print_status "Cleaning and compiling..."
mvn clean compile
print_success "Compilation completed"

# Run tests
print_status "Running tests..."
mvn test
print_success "Tests completed"

# Package application
print_status "Packaging application..."
mvn package -DskipTests
print_success "Packaging completed"

# Build Docker image
print_status "Building Docker image..."
docker build -t payment-processing-platform:latest .
print_success "Docker image built successfully"

# Tag for different environments
docker tag payment-processing-platform:latest payment-processing-platform:dev
docker tag payment-processing-platform:latest payment-processing-platform:$(date +%Y%m%d-%H%M%S)

print_success "🎉 Build completed successfully!"
print_status "Available images:"
docker images | grep payment-processing-platform

echo ""
print_status "Next steps:"
echo "1. Run locally: docker-compose up -d"
echo "2. Deploy to K8s: kubectl apply -f k8s/"
echo "3. Push to registry: docker push <registry>/payment-processing-platform:latest"