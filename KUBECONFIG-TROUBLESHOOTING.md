# Kubeconfig Troubleshooting Guide

This guide helps you resolve kubeconfig issues in GitHub Actions deployment.

## 🚨 Common Error: "json parse error"

### Error Message
```
error: error loading config file "kubeconfig": couldn't get version/kind; json parse error: json: cannot unmarshal string into Go value of type struct { APIVersion string "json:\"apiVersion,omitempty\""; Kind string "json:\"kind,omitempty\"" }
```

### Root Causes
1. **Base64 encoding issues**: The kubeconfig wasn't properly base64 encoded
2. **Corrupted secret**: The GitHub secret contains invalid data
3. **Wrong format**: The kubeconfig is in the wrong format
4. **ngrok URL mismatch**: The ngrok URL changed but kubeconfig wasn't updated

## 🔧 Step-by-Step Fix

### Step 1: Generate Correct Kubeconfig
```bash
# 1. Start ngrok with correct configuration
ngrok start --all --config=ngrok.yml

# 2. Generate kubeconfig for GitHub Actions
./generate-github-kubeconfig.sh

# 3. Copy the base64 encoded output
```

### Step 2: Update GitHub Secret
1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Find the **KUBECONFIG** secret
4. Click **Update**
5. Paste the new base64 encoded kubeconfig
6. Click **Update secret**

### Step 3: Test the Fix
```bash
# Test locally first
./test-ngrok-kubemini.sh

# Then trigger GitHub Actions deployment
git add .
git commit -m "Fix kubeconfig"
git push origin main
```

## 🧪 Validation Steps

### 1. Validate Base64 Encoding
```bash
# Test if the base64 string is valid
echo "YOUR_BASE64_STRING" | base64 -d > test-kubeconfig.yaml

# Check if it's a valid YAML
head -1 test-kubeconfig.yaml | grep -q "apiVersion:"
echo "✅ Valid kubeconfig format"
```

### 2. Test Kubeconfig Locally
```bash
# Test with the generated kubeconfig
KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get nodes
```

### 3. Verify GitHub Secret
The GitHub Actions workflow now includes validation:
- Checks if secret exists
- Validates base64 decoding
- Verifies kubeconfig format
- Tests cluster connectivity

## 🔍 Debugging Commands

### Check ngrok Status
```bash
# Check if ngrok is running
curl -s http://localhost:4040/api/tunnels | jq '.'

# Get TCP tunnel URL
curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[] | select(.proto == "tcp") | .public_url'
```

### Validate Kubeconfig Content
```bash
# Check kubeconfig structure
cat ngrok-kubeconfig.yaml | head -20

# Validate YAML syntax
python3 -c "import yaml; yaml.safe_load(open('ngrok-kubeconfig.yaml'))"
```

### Test Cluster Connectivity
```bash
# Test with verbose output
KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info --v=6

# Test with timeout
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get nodes --request-timeout=30s
```

## 🚨 Emergency Fixes

### If ngrok URL Changed
```bash
# 1. Get new ngrok URL
curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[] | select(.proto == "tcp") | .public_url'

# 2. Regenerate kubeconfig
./generate-github-kubeconfig.sh tcp://NEW_URL

# 3. Update GitHub secret with new base64 string
```

### If Secret is Completely Broken
```bash
# 1. Delete the secret in GitHub
# 2. Generate new kubeconfig
./generate-github-kubeconfig.sh

# 3. Add new secret with the base64 string
```

### If ngrok is Not Running
```bash
# 1. Start ngrok
ngrok start --all --config=ngrok.yml

# 2. Wait for tunnels to be ready
sleep 5

# 3. Generate kubeconfig
./generate-github-kubeconfig.sh
```

## 📋 Checklist

Before deploying to GitHub Actions:

- [ ] ngrok is running with correct configuration
- [ ] ngrok TCP tunnel is accessible
- [ ] kubeconfig works locally
- [ ] base64 encoding is valid
- [ ] GitHub secret is updated
- [ ] GitHub Actions workflow has proper error handling

## 🔧 Configuration Files

### ngrok.yml (Updated)
```yaml
version: "2"
tunnels:
  k8s-api:
    proto: tcp
    addr: 8443  # Changed from 52656
  backend-app:
    proto: http
    addr: 30080  # Changed from 30081
```

### GitHub Actions Workflow (Updated)
The workflow now includes:
- Secret validation
- Base64 decoding validation
- Kubeconfig format validation
- Better error messages
- Increased timeouts

## 🆘 Still Having Issues?

1. **Check GitHub Actions logs** for detailed error messages
2. **Verify ngrok is running** and accessible
3. **Test kubeconfig locally** before deploying
4. **Ensure minikube is running** and accessible
5. **Check network connectivity** to ngrok tunnel

## 📞 Support Commands

```bash
# Full diagnostic
./test-ngrok-kubemini.sh

# Generate fresh kubeconfig
./generate-github-kubeconfig.sh

# Check ngrok status
curl -s http://localhost:4040/api/tunnels | jq '.'

# Test local connectivity
KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info --v=6
```
