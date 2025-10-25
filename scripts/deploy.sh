#!/bin/bash

# Deployment script for Payment Processing Platform

set -e

echo "🚀 Deploying Payment Processing Platform..."

ENVIRONMENT=${1:-dev}
NAMESPACE="payment-platform"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
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

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed. Please install kubectl first."
    exit 1
fi

# Check if we can connect to Kubernetes cluster
if ! kubectl cluster-info &> /dev/null; then
    print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    exit 1
fi

print_success "Connected to Kubernetes cluster"

# Create namespace if it doesn't exist
print_status "Creating namespace: $NAMESPACE"
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Deploy the application
print_status "Deploying Payment Processing Platform to $ENVIRONMENT environment..."
kubectl apply -f k8s/payment-platform-deployment.yaml

# Wait for deployment to be ready
print_status "Waiting for deployment to be ready..."
kubectl wait --for=condition=available deployment/payment-processing-platform -n $NAMESPACE --timeout=300s

# Check deployment status
print_status "Deployment Status:"
kubectl get pods -n $NAMESPACE
kubectl get services -n $NAMESPACE
kubectl get hpa -n $NAMESPACE

print_success "🎉 Deployment completed successfully!"

# Get service endpoint
SERVICE_IP=$(kubectl get service payment-service -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
if [ -z "$SERVICE_IP" ]; then
    SERVICE_IP=$(kubectl get service payment-service -n $NAMESPACE -o jsonpath='{.spec.clusterIP}')
fi

echo ""
print_status "Service Information:"
echo "Service IP: $SERVICE_IP"
echo "Health Check: http://$SERVICE_IP:8080/api/v1/payments/health"
echo "Metrics: http://$SERVICE_IP:8080/actuator/prometheus"

echo ""
print_status "Useful commands:"
echo "kubectl logs -f deployment/payment-processing-platform -n $NAMESPACE"
echo "kubectl port-forward service/payment-service 8080:8080 -n $NAMESPACE"
echo "kubectl scale deployment payment-processing-platform --replicas=5 -n $NAMESPACE"