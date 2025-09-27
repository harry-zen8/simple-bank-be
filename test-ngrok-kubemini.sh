#!/bin/bash

# Test script to verify kubemini (minikube) exposed via ngrok is working
# Usage: ./test-ngrok-kubemini.sh [ngrok-url]

set -e

NGROK_URL="$1"
NAMESPACE="simple-bank"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🧪 Testing kubemini (minikube) exposed via ngrok${NC}"
echo "=================================================="

# Function to get ngrok tunnel URL from local API
get_ngrok_tcp_url() {
    if ! curl -s http://localhost:4040/api/tunnels > /dev/null 2>&1; then
        echo -e "${RED}❌ Error: ngrok is not running on localhost:4040${NC}"
        echo -e "${YELLOW}Please start ngrok first with: ngrok start --all --config=ngrok.yml${NC}"
        exit 1
    fi
    
    local tunnel_url=$(curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[] | select(.proto == "tcp") | .public_url' | head -1)
    
    if [ -z "$tunnel_url" ] || [ "$tunnel_url" = "null" ]; then
        echo -e "${RED}❌ No TCP tunnel found in ngrok${NC}"
        echo -e "${YELLOW}Available tunnels:${NC}"
        curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[] | "\(.name): \(.proto) -> \(.public_url)"'
        exit 1
    fi
    
    echo "$tunnel_url"
}

# Check if ngrok URL is provided, otherwise auto-detect
if [ -z "$NGROK_URL" ]; then
    echo -e "${BLUE}🔍 Auto-detecting ngrok TCP tunnel...${NC}"
    NGROK_URL=$(get_ngrok_tcp_url)
    echo -e "${GREEN}✅ Found TCP tunnel: $NGROK_URL${NC}"
else
    echo -e "${BLUE}Using provided ngrok URL: $NGROK_URL${NC}"
fi

echo -e "${BLUE}Using ngrok URL: $NGROK_URL${NC}"
echo ""

# Step 1: Test kubectl connection through ngrok
echo -e "${BLUE}1️⃣ Testing kubectl connection through ngrok...${NC}"
echo "----------------------------------------"

# Check if ngrok-kubeconfig.yaml exists
if [ ! -f "ngrok-kubeconfig.yaml" ]; then
    echo -e "${YELLOW}⚠️  ngrok-kubeconfig.yaml not found. Creating it...${NC}"
    if [ -f "create-ngrok-kubeconfig.sh" ]; then
        ./create-ngrok-kubeconfig.sh "$NGROK_URL"
    else
        echo -e "${RED}❌ create-ngrok-kubeconfig.sh not found${NC}"
        exit 1
    fi
fi

# Test kubectl with ngrok kubeconfig
echo -e "${BLUE}Testing kubectl cluster-info...${NC}"
if KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info > /dev/null 2>&1; then
    echo -e "${GREEN}✅ kubectl connection successful${NC}"
    KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info
else
    echo -e "${RED}❌ kubectl connection failed${NC}"
    echo -e "${YELLOW}Make sure:${NC}"
    echo "  - ngrok is running and accessible"
    echo "  - The ngrok URL is correct"
    echo "  - The kubeconfig is properly generated"
    exit 1
fi

echo ""

# Step 2: Check cluster and node status
echo -e "${BLUE}2️⃣ Checking cluster and node status...${NC}"
echo "----------------------------------------"

echo -e "${BLUE}Nodes:${NC}"
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get nodes -o wide

echo ""
echo -e "${BLUE}Namespaces:${NC}"
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get namespaces

echo ""

# Step 3: Check if application is deployed
echo -e "${BLUE}3️⃣ Checking application deployment...${NC}"
echo "----------------------------------------"

# Check if namespace exists
if KUBECONFIG=ngrok-kubeconfig.yaml kubectl get namespace $NAMESPACE > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Namespace '$NAMESPACE' exists${NC}"
    
    echo -e "${BLUE}Deployments:${NC}"
    KUBECONFIG=ngrok-kubeconfig.yaml kubectl get deployments -n $NAMESPACE
    
    echo ""
    echo -e "${BLUE}Services:${NC}"
    KUBECONFIG=ngrok-kubeconfig.yaml kubectl get services -n $NAMESPACE
    
    echo ""
    echo -e "${BLUE}Pods:${NC}"
    KUBECONFIG=ngrok-kubeconfig.yaml kubectl get pods -n $NAMESPACE
    
    echo ""
    echo -e "${BLUE}Ingress:${NC}"
    KUBECONFIG=ngrok-kubeconfig.yaml kubectl get ingress -n $NAMESPACE 2>/dev/null || echo "No ingress found"
    
else
    echo -e "${YELLOW}⚠️  Namespace '$NAMESPACE' not found${NC}"
    echo -e "${YELLOW}Deploy the application first using: ./scripts/deploy.sh${NC}"
fi

echo ""

# Step 4: Test application endpoints via ngrok HTTP tunnel
echo -e "${BLUE}4️⃣ Testing application endpoints via ngrok HTTP tunnel${NC}"
echo "----------------------------------------"

# Get ngrok HTTP tunnel URL
NGROK_HTTP_URL=$(curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[] | select(.proto == "https") | .public_url' | head -1)

if [ -n "$NGROK_HTTP_URL" ] && [ "$NGROK_HTTP_URL" != "null" ]; then
    echo -e "${BLUE}ngrok HTTP tunnel: $NGROK_HTTP_URL${NC}"
    
    # Test health endpoint via ngrok
    echo -e "${BLUE}Testing health endpoint via ngrok...${NC}"
    if curl -s -f "$NGROK_HTTP_URL/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Health endpoint accessible via ngrok${NC}"
        curl -s "$NGROK_HTTP_URL/actuator/health" | jq . 2>/dev/null || curl -s "$NGROK_HTTP_URL/actuator/health"
    else
        echo -e "${RED}❌ Health endpoint not accessible via ngrok${NC}"
    fi
    
    # Test Swagger UI via ngrok
    echo -e "${BLUE}Testing Swagger UI via ngrok...${NC}"
    if curl -s -f "$NGROK_HTTP_URL/swagger-ui/index.html" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Swagger UI accessible via ngrok${NC}"
        echo -e "${BLUE}   URL: $NGROK_HTTP_URL/swagger-ui/index.html${NC}"
    else
        echo -e "${RED}❌ Swagger UI not accessible via ngrok${NC}"
    fi
    
    # Test API docs via ngrok
    echo -e "${BLUE}Testing API docs via ngrok...${NC}"
    if curl -s -f "$NGROK_HTTP_URL/v3/api-docs" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ API docs accessible via ngrok${NC}"
        echo -e "${BLUE}   URL: $NGROK_HTTP_URL/v3/api-docs${NC}"
    else
        echo -e "${RED}❌ API docs not accessible via ngrok${NC}"
    fi
    
else
    echo -e "${RED}❌ No ngrok HTTP tunnel found${NC}"
    echo -e "${YELLOW}   Make sure ngrok is running with HTTP tunnel configured${NC}"
fi

echo ""

# Step 5: Test port forwarding as alternative
echo -e "${BLUE}5️⃣ Testing port forwarding...${NC}"
echo "----------------------------------------"

echo -e "${BLUE}Starting port forward test (will run for 10 seconds)...${NC}"
timeout 10s KUBECONFIG=ngrok-kubeconfig.yaml kubectl port-forward service/simple-bank-service 8080:8080 -n $NAMESPACE &
PORT_FORWARD_PID=$!

sleep 3

# Test local port forward
if curl -s -f "http://localhost:8080/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Port forwarding works${NC}"
    echo -e "${BLUE}   Local URL: http://localhost:8080${NC}"
else
    echo -e "${RED}❌ Port forwarding failed${NC}"
fi

# Clean up port forward
kill $PORT_FORWARD_PID 2>/dev/null || true

echo ""

# Step 6: Summary and recommendations
echo -e "${BLUE}6️⃣ Summary and Access Information${NC}"
echo "=================================================="

# Get ngrok HTTP URL for summary
NGROK_HTTP_URL_SUMMARY=$(curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[] | select(.proto == "https") | .public_url' | head -1)

if [ -n "$NGROK_HTTP_URL_SUMMARY" ] && [ "$NGROK_HTTP_URL_SUMMARY" != "null" ]; then
    echo -e "${GREEN}🎉 kubemini via ngrok is working!${NC}"
    echo ""
    echo -e "${BLUE}📋 Public Access URLs (via ngrok):${NC}"
    echo -e "${GREEN}   Application: $NGROK_HTTP_URL_SUMMARY${NC}"
    echo -e "${GREEN}   Health Check: $NGROK_HTTP_URL_SUMMARY/actuator/health${NC}"
    echo -e "${GREEN}   Swagger UI: $NGROK_HTTP_URL_SUMMARY/swagger-ui/index.html${NC}"
    echo -e "${GREEN}   API Docs: $NGROK_HTTP_URL_SUMMARY/v3/api-docs${NC}"
    
    echo ""
    echo -e "${BLUE}🔧 kubectl commands:${NC}"
    echo -e "${YELLOW}   KUBECONFIG=ngrok-kubeconfig.yaml kubectl get pods -n $NAMESPACE${NC}"
    echo -e "${YELLOW}   KUBECONFIG=ngrok-kubeconfig.yaml kubectl logs -f deployment/simple-bank-app -n $NAMESPACE${NC}"
    echo -e "${YELLOW}   KUBECONFIG=ngrok-kubeconfig.yaml kubectl port-forward service/simple-bank-service 8080:8080 -n $NAMESPACE${NC}"
    
else
    echo -e "${RED}❌ kubemini via ngrok has issues${NC}"
    echo ""
    echo -e "${YELLOW}🔧 Troubleshooting steps:${NC}"
    echo "1. Ensure ngrok is running: ngrok tcp 8443"
    echo "2. Verify the ngrok URL is correct"
    echo "3. Check if the application is deployed: ./scripts/deploy.sh"
    echo "4. Verify kubectl can connect: KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info"
    echo "5. Check pod status: KUBECONFIG=ngrok-kubeconfig.yaml kubectl get pods -n $NAMESPACE"
fi

echo ""
echo -e "${BLUE}📝 Notes:${NC}"
echo "- Keep ngrok running while using kubectl"
echo "- The ngrok URL may change if you restart ngrok"
echo "- Use the generated ngrok-kubeconfig.yaml for kubectl access"
echo "- For GitHub Actions, use the base64 encoded kubeconfig as a secret"
