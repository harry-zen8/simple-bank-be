#!/bin/bash

# Script to create a CI/CD-ready kubeconfig for minikube with ngrok (insecure)
# This disables TLS verification to work with ngrok

set -e

if [ $# -eq 0 ]; then
    echo "❌ Please provide ngrok URL"
    echo "Usage: $0 <ngrok-url>"
    echo "Example: $0 0.tcp.ap.ngrok.io:11374"
    exit 1
fi

NGROK_URL="$1"
echo "🔧 Creating insecure kubeconfig for ngrok URL: $NGROK_URL"

# Check if minikube is running
if ! minikube status >/dev/null 2>&1; then
    echo "❌ Minikube is not running. Please start it first:"
    echo "   minikube start"
    exit 1
fi

# Get the certificate data
echo "📜 Reading certificates..."
CA_CERT=$(cat ~/.minikube/ca.crt | base64 | tr -d '\n')
CLIENT_CERT=$(cat ~/.minikube/profiles/minikube/client.crt | base64 | tr -d '\n')
CLIENT_KEY=$(cat ~/.minikube/profiles/minikube/client.key | base64 | tr -d '\n')

echo "✅ Certificates read successfully"

# Create the kubeconfig content with insecure-skip-tls-verify
KUBECONFIG_CONTENT=$(cat << EOF
apiVersion: v1
kind: Config
clusters:
- cluster:
    certificate-authority-data: $CA_CERT
    server: https://$NGROK_URL
    insecure-skip-tls-verify: true
  name: minikube
contexts:
- context:
    cluster: minikube
    user: minikube
  name: minikube
current-context: minikube
users:
- name: minikube
  user:
    client-certificate-data: $CLIENT_CERT
    client-key-data: $CLIENT_KEY
EOF
)

# Save to file
echo "$KUBECONFIG_CONTENT" > minikube-ngrok-insecure-kubeconfig.yaml
echo "✅ Created insecure kubeconfig: minikube-ngrok-insecure-kubeconfig.yaml"

# Test the kubeconfig
echo "🧪 Testing kubeconfig..."
export KUBECONFIG=minikube-ngrok-insecure-kubeconfig.yaml
if kubectl get nodes --request-timeout=10s >/dev/null 2>&1; then
    echo "✅ Kubeconfig works! Nodes:"
    kubectl get nodes
else
    echo "❌ Kubeconfig test failed"
    echo "Make sure ngrok is running: ngrok tcp 52656"
    exit 1
fi

# Base64 encode for GitHub secrets
echo ""
echo "🔐 Base64 encoded kubeconfig for GitHub secrets:"
echo "================================================"
echo "$KUBECONFIG_CONTENT" | base64 | tr -d '\n'
echo ""
echo "================================================"

echo ""
echo "📋 Next steps:"
echo "1. Make sure ngrok is running: ngrok tcp 52656"
echo "2. Copy the base64 output above"
echo "3. Update your GitHub KUBECONFIG secret with the new value"
echo "4. Your CI/CD pipeline will now work with minikube through ngrok!"

# Clean up
unset KUBECONFIG

echo ""
echo "🎉 Done! Your minikube kubeconfig is ready for ngrok + CI/CD (insecure mode)."
