#!/bin/bash

# Simple script to create a CI/CD-ready kubeconfig for minikube
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

# Get the certificate data
echo "📜 Reading certificates..."
CA_CERT=$(cat ~/.minikube/ca.crt | base64 | tr -d '\n')
CLIENT_CERT=$(cat ~/.minikube/profiles/minikube/client.crt | base64 | tr -d '\n')
CLIENT_KEY=$(cat ~/.minikube/profiles/minikube/client.key | base64 | tr -d '\n')

echo "✅ Certificates read successfully"

# Create the kubeconfig content
KUBECONFIG_CONTENT=$(cat << EOF
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
)

# Save to file
echo "$KUBECONFIG_CONTENT" > minikube-cicd-kubeconfig.yaml
echo "✅ Created kubeconfig: minikube-cicd-kubeconfig.yaml"

# Base64 encode for GitHub secrets
echo ""
echo "🔐 Base64 encoded kubeconfig for GitHub secrets:"
echo "================================================"
echo "$KUBECONFIG_CONTENT" | base64 | tr -d '\n'
echo ""
echo "================================================"

echo ""
echo "📋 Next steps:"
echo "1. Copy the base64 output above"
echo "2. Go to GitHub → Settings → Secrets and variables → Actions"
echo "3. Update the KUBECONFIG secret with the base64 value"
echo "4. Your CI/CD pipeline will now work with minikube!"

echo ""
echo "🎉 Done! Your minikube kubeconfig is ready for CI/CD."
