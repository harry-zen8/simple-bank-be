#!/bin/bash

# Deploy script for Simple Bank Application
# Usage: ./scripts/deploy.sh [environment] [image-tag] [docker-username]

set -e

ENVIRONMENT=${1:-base}
IMAGE_TAG=${2:-latest}
NAMESPACE="simple-bank"

# Get DOCKER_USERNAME from image-config.yaml if not provided as parameter
if [ -n "$3" ]; then
    DOCKER_USERNAME=$3
else
    # Try to get from environment variable first
    if [ -n "$DOCKER_USERNAME" ]; then
        echo "Using DOCKER_USERNAME from environment: $DOCKER_USERNAME"
    else
        # Extract from image-config.yaml
        DOCKER_USERNAME=$(grep "DOCKER_USERNAME:" k8s/image-config.yaml | sed 's/.*DOCKER_USERNAME: "\(.*\)".*/\1/')
        if [ -z "$DOCKER_USERNAME" ] || [ "$DOCKER_USERNAME" = "your-username" ]; then
            echo "Error: DOCKER_USERNAME not set in environment or image-config.yaml"
            echo "Please set DOCKER_USERNAME environment variable or update k8s/image-config.yaml"
            exit 1
        fi
        echo "Using DOCKER_USERNAME from image-config.yaml: $DOCKER_USERNAME"
    fi
fi

echo "Deploying Simple Bank Application..."
echo "Environment: $ENVIRONMENT"
echo "Image Tag: $IMAGE_TAG"
echo "Docker Username: $DOCKER_USERNAME"
echo "Namespace: $NAMESPACE"

# Set Docker image name
DOCKER_IMAGE_NAME="docker.io/$DOCKER_USERNAME/simple-bank-be:$IMAGE_TAG"
echo "Using Docker image: $DOCKER_IMAGE_NAME"

# Create namespace if it doesn't exist
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Generate deployment from template
echo "Generating deployment from template..."
export DOCKER_IMAGE_NAME
envsubst < k8s/deployment-template.yaml > k8s/deployment-generated.yaml

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if kubeconfig is set
if [ -z "$KUBECONFIG" ] && [ ! -f ~/.kube/config ]; then
    echo "KUBECONFIG is not set and ~/.kube/config does not exist"
    exit 1
fi

# Create namespace if it doesn't exist
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Deploy based on environment
if [ "$ENVIRONMENT" = "production" ]; then
    echo "Deploying to production environment..."
    # Apply base resources first
    kubectl apply -f k8s/namespace.yaml
    kubectl apply -f k8s/configmap.yaml
    kubectl apply -f k8s/secret.yaml
    kubectl apply -f k8s/postgres-deployment.yaml
    kubectl apply -f k8s/postgres-service.yaml
    kubectl apply -f k8s/service.yaml
    kubectl apply -f k8s/ingress.yaml
    kubectl apply -f k8s/ingress-controller.yaml
    # Apply generated deployment
    kubectl apply -f k8s/deployment-generated.yaml
    # Scale for production
    kubectl scale deployment simple-bank-app --replicas=3 -n $NAMESPACE
elif [ "$ENVIRONMENT" = "staging" ]; then
    echo "Deploying to staging environment..."
    # Apply base resources first
    kubectl apply -f k8s/namespace.yaml
    kubectl apply -f k8s/configmap.yaml
    kubectl apply -f k8s/secret.yaml
    kubectl apply -f k8s/postgres-deployment.yaml
    kubectl apply -f k8s/postgres-service.yaml
    kubectl apply -f k8s/service.yaml
    kubectl apply -f k8s/ingress.yaml
    kubectl apply -f k8s/ingress-controller.yaml
    # Apply generated deployment
    kubectl apply -f k8s/deployment-generated.yaml
else
    echo "Deploying to base environment..."
    # Apply base resources first
    kubectl apply -f k8s/namespace.yaml
    kubectl apply -f k8s/configmap.yaml
    kubectl apply -f k8s/secret.yaml
    kubectl apply -f k8s/postgres-deployment.yaml
    kubectl apply -f k8s/postgres-service.yaml
    kubectl apply -f k8s/service.yaml
    kubectl apply -f k8s/ingress.yaml
    kubectl apply -f k8s/ingress-controller.yaml
    # Apply generated deployment
    kubectl apply -f k8s/deployment-generated.yaml
fi

# Wait for deployment to be ready
echo "Waiting for deployment to be ready..."
kubectl rollout status deployment/simple-bank-app -n $NAMESPACE --timeout=300s

# Get service information
echo "Getting service information..."
kubectl get services -n $NAMESPACE

# Get public access information
echo ""
echo "=== Public Access Information ==="
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="ExternalIP")].address}')
if [ -z "$NODE_IP" ]; then
  NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
fi

NODEPORT=$(kubectl get service simple-bank-service -n $NAMESPACE -o jsonpath='{.spec.ports[0].nodePort}')
INGRESS_NODEPORT=$(kubectl get service ingress-nginx-controller -n ingress-nginx -o jsonpath='{.spec.ports[0].nodePort}')

echo "🌐 Direct Application Access (NodePort):"
echo "   http://$NODE_IP:$NODEPORT"
echo ""
echo "🌐 Application Access via Ingress:"
echo "   http://$NODE_IP:$INGRESS_NODEPORT"
echo ""
echo "📋 API Endpoints:"
echo "   Health Check: http://$NODE_IP:$NODEPORT/actuator/health"
echo "   Swagger UI: http://$NODE_IP:$NODEPORT/swagger-ui/index.html"
echo "   API Docs: http://$NODE_IP:$NODEPORT/v3/api-docs"
echo ""

echo "Deployment completed successfully!"
echo "To check the application status, run:"
echo "kubectl get pods -n $NAMESPACE"
echo "kubectl logs -f deployment/simple-bank-app -n $NAMESPACE"
echo ""
echo "🎉 Your Simple Bank API is now publicly accessible!"
