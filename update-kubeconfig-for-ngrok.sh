#!/bin/bash

# Script to update kubeconfig for ngrok tunnel
# Usage: ./update-kubeconfig-for-ngrok.sh <ngrok-url>
# Example: ./update-kubeconfig-for-ngrok.sh 0.tcp.ngrok.io:12345

set -e

if [ $# -eq 0 ]; then
    echo "❌ Please provide ngrok URL"
    echo "Usage: $0 <ngrok-url>"
    echo "Example: $0 0.tcp.ngrok.io:12345"
    exit 1
fi

NGROK_URL="$1"
echo "🔧 Updating kubeconfig for ngrok URL: $NGROK_URL"

# Read the current kubeconfig
KUBECONFIG_CONTENT=$(cat minikube-cicd-kubeconfig.yaml)

# Update the server URL
UPDATED_KUBECONFIG=$(echo "$KUBECONFIG_CONTENT" | sed "s|server: https://.*|server: https://$NGROK_URL|")

# Save the updated kubeconfig
echo "$UPDATED_KUBECONFIG" > minikube-ngrok-kubeconfig.yaml
echo "✅ Created ngrok kubeconfig: minikube-ngrok-kubeconfig.yaml"

# Base64 encode for GitHub secrets
echo ""
echo "🔐 Base64 encoded kubeconfig for GitHub secrets:"
echo "================================================"
echo "$UPDATED_KUBECONFIG" | base64 | tr -d '\n'
echo ""
echo "================================================"

echo ""
echo "📋 Next steps:"
echo "1. Make sure ngrok is running: ngrok tcp 52656"
echo "2. Copy the base64 output above"
echo "3. Update your GitHub KUBECONFIG secret with the new value"
echo "4. Your CI/CD pipeline will now work with minikube through ngrok!"

echo ""
echo "🎉 Done! Your minikube kubeconfig is ready for ngrok + CI/CD."
