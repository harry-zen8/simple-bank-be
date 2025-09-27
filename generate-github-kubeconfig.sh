#!/bin/bash

# Script to generate kubeconfig for GitHub Actions deployment
# Usage: ./generate-github-kubeconfig.sh [ngrok-url]

set -e

NGROK_URL="$1"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔧 Generating kubeconfig for GitHub Actions${NC}"
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

# Generate kubeconfig using existing script
echo -e "${BLUE}📝 Generating kubeconfig...${NC}"
if [ -f "create-ngrok-kubeconfig.sh" ]; then
    ./create-ngrok-kubeconfig.sh "$NGROK_URL"
else
    echo -e "${RED}❌ create-ngrok-kubeconfig.sh not found${NC}"
    exit 1
fi

# Validate the generated kubeconfig
echo -e "${BLUE}🔍 Validating kubeconfig...${NC}"
if [ ! -f "ngrok-kubeconfig.yaml" ]; then
    echo -e "${RED}❌ ngrok-kubeconfig.yaml not generated${NC}"
    exit 1
fi

# Test the kubeconfig locally
echo -e "${BLUE}🧪 Testing kubeconfig locally...${NC}"
if KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Kubeconfig works locally${NC}"
    KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info
else
    echo -e "${RED}❌ Kubeconfig test failed locally${NC}"
    echo -e "${YELLOW}Please check:${NC}"
    echo "  - ngrok is running and accessible"
    echo "  - The ngrok URL is correct"
    echo "  - minikube is running"
    exit 1
fi

# Generate base64 encoded kubeconfig for GitHub
echo -e "${BLUE}📦 Generating base64 encoded kubeconfig for GitHub...${NC}"
BASE64_KUBECONFIG=$(cat ngrok-kubeconfig.yaml | base64 -w 0)

# Validate base64 encoding
echo -e "${BLUE}🔍 Validating base64 encoding...${NC}"
if echo "$BASE64_KUBECONFIG" | base64 -d > /tmp/decoded-kubeconfig.yaml 2>/dev/null; then
    if diff ngrok-kubeconfig.yaml /tmp/decoded-kubeconfig.yaml > /dev/null; then
        echo -e "${GREEN}✅ Base64 encoding is valid${NC}"
        rm -f /tmp/decoded-kubeconfig.yaml
    else
        echo -e "${RED}❌ Base64 encoding corrupted the file${NC}"
        rm -f /tmp/decoded-kubeconfig.yaml
        exit 1
    fi
else
    echo -e "${RED}❌ Invalid base64 encoding${NC}"
    exit 1
fi

# Display results
echo ""
echo -e "${GREEN}🎉 Kubeconfig generated successfully!${NC}"
echo "=================================================="
echo ""
echo -e "${BLUE}📋 Next steps:${NC}"
echo ""
echo -e "${YELLOW}1. Add this to your GitHub repository secrets:${NC}"
echo "   Secret name: KUBECONFIG"
echo "   Secret value: (copy the base64 string below)"
echo ""
echo -e "${BLUE}Base64 encoded kubeconfig:${NC}"
echo "$BASE64_KUBECONFIG"
echo ""
echo -e "${YELLOW}2. GitHub Actions will use this kubeconfig to deploy${NC}"
echo ""
echo -e "${BLUE}📝 To add the secret:${NC}"
echo "   1. Go to your GitHub repository"
echo "   2. Click Settings → Secrets and variables → Actions"
echo "   3. Click 'New repository secret'"
echo "   4. Name: KUBECONFIG"
echo "   5. Value: (paste the base64 string above)"
echo "   6. Click 'Add secret'"
echo ""
echo -e "${BLUE}🧪 Test the deployment:${NC}"
echo "   - Push to main or develop branch"
echo "   - Check GitHub Actions logs"
echo "   - Use the test script: ./test-ngrok-kubemini.sh"
echo ""
echo -e "${YELLOW}⚠️  Important notes:${NC}"
echo "   - Keep ngrok running during deployment"
echo "   - The ngrok URL may change if you restart ngrok"
echo "   - Regenerate this kubeconfig if ngrok URL changes"
echo "   - This kubeconfig is for testing only (publicly accessible)"
