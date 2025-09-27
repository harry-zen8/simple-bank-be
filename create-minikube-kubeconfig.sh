#!/bin/bash

# Script to create a CI/CD-ready kubeconfig for minikube
# This embeds certificates directly instead of using file paths

set -e

echo "🔧 Creating CI/CD-ready kubeconfig for minikube..."

# Check if minikube is running
if ! minikube status >/dev/null 2>&1; then
    echo "❌ Minikube is not running. Please start it first:"
    echo "   minikube start"
    exit 1
fi

# Get minikube info
MINIKUBE_IP=$(minikube ip)
MINIKUBE_PORT=$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}' | sed 's/.*://')

echo "📍 Minikube IP: $MINIKUBE_IP"
echo "📍 Minikube Port: $MINIKUBE_PORT"

# Create a temporary kubeconfig with embedded certificates
TEMP_KUBECONFIG="/tmp/minikube-cicd-kubeconfig.yaml"

# Get the certificate data
CA_CERT=$(cat ~/.minikube/ca.crt | base64 | tr -d '\n')
CLIENT_CERT=$(cat ~/.minikube/profiles/minikube/client.crt | base64 | tr -d '\n')
CLIENT_KEY=$(cat ~/.minikube/profiles/minikube/client.key | base64 | tr -d '\n')

# Create the kubeconfig
cat > "$TEMP_KUBECONFIG" << EOF
apiVersion: v1
kind: Config
clusters:
- cluster:
    certificate-authority-data: $CA_CERT
    server: https://$MINIKUBE_IP:$MINIKUBE_PORT
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

echo "✅ Created kubeconfig: $TEMP_KUBECONFIG"

# Test the kubeconfig
echo "🧪 Testing kubeconfig..."
export KUBECONFIG="$TEMP_KUBECONFIG"
if kubectl get nodes >/dev/null 2>&1; then
    echo "✅ Kubeconfig works! Nodes:"
    kubectl get nodes
else
    echo "❌ Kubeconfig test failed"
    exit 1
fi

# Base64 encode for GitHub secrets
echo ""
echo "🔐 Base64 encoded kubeconfig for GitHub secrets:"
echo "================================================"
base64 -i "$TEMP_KUBECONFIG"
echo "================================================"

echo ""
echo "📋 Next steps:"
echo "1. Copy the base64 output above"
echo "2. Go to GitHub → Settings → Secrets and variables → Actions"
echo "3. Update the KUBECONFIG secret with the base64 value"
echo "4. Your CI/CD pipeline will now work with minikube!"

# Clean up
unset KUBECONFIG
rm -f "$TEMP_KUBECONFIG"

echo ""
echo "🎉 Done! Your minikube kubeconfig is ready for CI/CD."
