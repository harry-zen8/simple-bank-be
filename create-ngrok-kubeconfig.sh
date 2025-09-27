#!/bin/bash

# Get current minikube config
MINIKUBE_IP=$(kubectl config view -o jsonpath='{.clusters[0].cluster.server}')

# Function to get ngrok tunnel URL from local API
get_ngrok_url() {
    local tunnel_type="$1"  # "tcp" or "http"
    
    # Check if ngrok is running
    if ! curl -s http://localhost:4040/api/tunnels > /dev/null 2>&1; then
        echo "Error: ngrok is not running on localhost:4040"
        echo "Please start ngrok first with: ngrok start --all --config=ngrok.yml"
        exit 1
    fi
    
    # Get tunnel URL from ngrok API
    local tunnel_url
    if [ "$tunnel_type" = "tcp" ]; then
        tunnel_url=$(curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[] | select(.proto == "tcp") | .public_url' | head -1)
    else
        tunnel_url=$(curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[] | select(.proto == "http") | .public_url' | head -1)
    fi
    
    if [ -z "$tunnel_url" ] || [ "$tunnel_url" = "null" ]; then
        echo "Error: No $tunnel_type tunnel found in ngrok"
        echo "Available tunnels:"
        curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[] | "\(.name): \(.proto) -> \(.public_url)"'
        exit 1
    fi
    
    echo "$tunnel_url"
}

# Get ngrok URL (default to TCP for k8s API, but allow override)
NGROK_URL="$1"
if [ -z "$NGROK_URL" ]; then
    echo "Auto-detecting ngrok TCP tunnel..."
    NGROK_URL=$(get_ngrok_url "tcp")
    echo "Found TCP tunnel: $NGROK_URL"
else
    echo "Using provided ngrok URL: $NGROK_URL"
fi

# Convert TCP ngrok URL to https format for kubectl
NGROK_HOST=$(echo $NGROK_URL | sed 's|tcp://||' | sed 's|:.*||')
NGROK_PORT=$(echo $NGROK_URL | sed 's|.*:||')

# Get minikube certs (handle both file paths and base64 data)
CA_CERT_PATH=$(kubectl config view --raw -o jsonpath='{.clusters[0].cluster.certificate-authority}')
CA_CERT_DATA=$(kubectl config view --raw -o jsonpath='{.clusters[0].cluster.certificate-authority-data}')

CLIENT_CERT_PATH=$(kubectl config view --raw -o jsonpath='{.users[0].user.client-certificate}')
CLIENT_CERT_DATA=$(kubectl config view --raw -o jsonpath='{.users[0].user.client-certificate-data}')

CLIENT_KEY_PATH=$(kubectl config view --raw -o jsonpath='{.users[0].user.client-key}')
CLIENT_KEY_DATA=$(kubectl config view --raw -o jsonpath='{.users[0].user.client-key-data}')

# Use base64 data if available, otherwise convert from file paths
if [ -n "$CA_CERT_DATA" ]; then
    CA_CERT="$CA_CERT_DATA"
else
    CA_CERT=$(base64 -i "$CA_CERT_PATH" | tr -d '\n')
fi

if [ -n "$CLIENT_CERT_DATA" ]; then
    CLIENT_CERT="$CLIENT_CERT_DATA"
else
    CLIENT_CERT=$(base64 -i "$CLIENT_CERT_PATH" | tr -d '\n')
fi

if [ -n "$CLIENT_KEY_DATA" ]; then
    CLIENT_KEY="$CLIENT_KEY_DATA"
else
    CLIENT_KEY=$(base64 -i "$CLIENT_KEY_PATH" | tr -d '\n')
fi

# Create new kubeconfig with ngrok endpoint
cat > ngrok-kubeconfig.yaml << EOF
apiVersion: v1
kind: Config
clusters:
- cluster:
    server: https://${NGROK_HOST}:${NGROK_PORT}
    insecure-skip-tls-verify: true
  name: minikube-ngrok
contexts:
- context:
    cluster: minikube-ngrok
    user: minikube
  name: minikube-ngrok
current-context: minikube-ngrok
users:
- name: minikube
  user:
    client-certificate-data: ${CLIENT_CERT}
    client-key-data: ${CLIENT_KEY}
EOF

# Base64 encode for GitHub secret
echo ""
echo "=== Kubeconfig created: ngrok-kubeconfig.yaml ==="
echo ""
echo "Base64 encoded (add this to GitHub Secret KUBECONFIG):"
echo ""
cat ngrok-kubeconfig.yaml | base64
echo ""
echo "Keep ngrok running! GitHub Actions needs it to connect."