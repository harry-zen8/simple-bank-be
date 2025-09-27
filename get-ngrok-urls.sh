#!/bin/bash

# Script to get ngrok tunnel URLs from local API
# Usage: ./get-ngrok-urls.sh [tcp|http|all]

TUNNEL_TYPE="${1:-all}"

# Check if ngrok is running
if ! curl -s http://localhost:4040/api/tunnels > /dev/null 2>&1; then
    echo "❌ Error: ngrok is not running on localhost:4040"
    echo "Please start ngrok first with: ngrok start --all --config=ngrok.yml"
    exit 1
fi

# Function to get tunnel URL by type
get_tunnel_url() {
    local tunnel_type="$1"
    curl -s http://localhost:4040/api/tunnels | jq -r ".tunnels[] | select(.proto == \"$tunnel_type\") | .public_url" | head -1
}

# Function to get tunnel name and URL
get_tunnel_info() {
    local tunnel_type="$1"
    curl -s http://localhost:4040/api/tunnels | jq -r ".tunnels[] | select(.proto == \"$tunnel_type\") | \"\(.name): \(.public_url)\"" | head -1
}

echo "🔍 Fetching ngrok tunnel information..."
echo ""

case "$TUNNEL_TYPE" in
    "tcp")
        TCP_URL=$(get_tunnel_url "tcp")
        if [ -n "$TCP_URL" ] && [ "$TCP_URL" != "null" ]; then
            echo "🔧 Kubernetes API (TCP): $TCP_URL"
        else
            echo "❌ No TCP tunnel found"
        fi
        ;;
    "http")
        HTTP_URL=$(get_tunnel_url "http")
        if [ -n "$HTTP_URL" ] && [ "$HTTP_URL" != "null" ]; then
            echo "🌐 Backend App (HTTP): $HTTP_URL"
        else
            echo "❌ No HTTP tunnel found"
        fi
        ;;
    "all"|*)
        echo "📋 All Active Tunnels:"
        echo ""
        
        # Get all tunnel information
        TCP_INFO=$(get_tunnel_info "tcp")
        HTTP_INFO=$(get_tunnel_info "http")
        
        if [ -n "$TCP_INFO" ] && [ "$TCP_INFO" != "null" ]; then
            echo "🔧 $TCP_INFO"
        else
            echo "❌ No TCP tunnel found"
        fi
        
        if [ -n "$HTTP_INFO" ] && [ "$HTTP_INFO" != "null" ]; then
            echo "🌐 $HTTP_INFO"
        else
            echo "❌ No HTTP tunnel found"
        fi
        
        echo ""
        echo "📝 Quick Access URLs:"
        TCP_URL=$(get_tunnel_url "tcp")
        HTTP_URL=$(get_tunnel_url "http")
        
        if [ -n "$TCP_URL" ] && [ "$TCP_URL" != "null" ]; then
            echo "export NGROK_TCP_URL=\"$TCP_URL\""
        fi
        
        if [ -n "$HTTP_URL" ] && [ "$HTTP_URL" != "null" ]; then
            echo "export NGROK_HTTP_URL=\"$HTTP_URL\""
            echo ""
            echo "🧪 Test your backend:"
            echo "curl -s $HTTP_URL/actuator/health"
        fi
        ;;
esac

echo ""
