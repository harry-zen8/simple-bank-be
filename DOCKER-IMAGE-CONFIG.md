# Docker Image Configuration Guide

This guide explains how to configure Docker image names in a centralized way for the Simple Bank application.

## 🎯 Centralized Configuration

Instead of hardcoding Docker image names in multiple places, we now use a **single configuration approach**:

### 1. **Template-Based Approach** (Recommended)
- Uses `k8s/deployment-template.yaml` with environment variable substitution
- Docker image name is set via `DOCKER_IMAGE_NAME` environment variable
- Generated deployment file: `k8s/deployment-generated.yaml`

### 2. **ConfigMap Approach** (Alternative)
- Uses `k8s/image-config.yaml` for centralized configuration
- Can be updated dynamically without code changes

## 🔧 Configuration Methods

### Method 1: Environment Variables (GitHub Actions)
The GitHub Actions workflow automatically sets the Docker image name based on:
- **Repository name**: `${{ env.IMAGE_NAME }}`
- **Registry**: `${{ env.REGISTRY }}` (docker.io)
- **Tag**: `stable` for main branch, `develop` for develop branch

**Example**: `docker.io/your-username/simple-bank:stable`

### Method 2: Manual Deployment Script
```bash
# Deploy with custom Docker username and tag
./scripts/deploy.sh production stable your-docker-username

# Deploy with default settings
./scripts/deploy.sh
```

### Method 3: Direct Environment Variable
```bash
export DOCKER_IMAGE_NAME="docker.io/your-username/simple-bank:latest"
envsubst < k8s/deployment-template.yaml > k8s/deployment-generated.yaml
kubectl apply -f k8s/deployment-generated.yaml
```

## 📁 File Structure

```
k8s/
├── deployment-template.yaml      # Template with ${DOCKER_IMAGE_NAME}
├── deployment-generated.yaml     # Generated deployment (gitignored)
├── image-config.yaml            # ConfigMap for image settings
└── ...
```

## 🚀 Quick Setup

### 1. Update Your Docker Username
Edit the deployment script default:
```bash
# In scripts/deploy.sh, line 10
DOCKER_USERNAME=${3:-"your-actual-username"}
```

### 2. Or Use Environment Variable
```bash
export DOCKER_USERNAME="your-actual-username"
./scripts/deploy.sh
```

### 3. Or Pass as Parameter
```bash
./scripts/deploy.sh base latest your-actual-username
```

## 🔄 How It Works

### GitHub Actions Workflow
1. **Build Stage**: Creates Docker image with tag based on branch
2. **Deploy Stage**: 
   - Sets `DOCKER_IMAGE_NAME` environment variable
   - Generates deployment from template using `envsubst`
   - Applies all Kubernetes manifests

### Manual Deployment
1. **Script Execution**: Sets Docker image name from parameters
2. **Template Processing**: Uses `envsubst` to substitute variables
3. **Kubernetes Apply**: Deploys the generated configuration

## 📋 Configuration Examples

### Development
```bash
DOCKER_IMAGE_NAME="docker.io/myusername/simple-bank:dev"
```

### Staging
```bash
DOCKER_IMAGE_NAME="docker.io/myusername/simple-bank:develop"
```

### Production
```bash
DOCKER_IMAGE_NAME="docker.io/myusername/simple-bank:stable"
```

### Custom Registry
```bash
DOCKER_IMAGE_NAME="myregistry.com/myusername/simple-bank:v1.0.0"
```

## 🛠️ Advanced Configuration

### Custom Image Names
If you want to use a different image name pattern:

1. **Update the deployment script**:
```bash
# In scripts/deploy.sh
IMAGE_NAME=${4:-"simple-bank"}
DOCKER_IMAGE_NAME="docker.io/$DOCKER_USERNAME/$IMAGE_NAME:$IMAGE_TAG"
```

2. **Update GitHub Actions**:
```yaml
# In .github/workflows/ci-cd.yml
DOCKER_IMAGE_NAME="${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.ref_name == 'main' && 'stable' || 'develop' }}"
```

### Multiple Environments
For different environments with different image names:

```bash
# Production
DOCKER_IMAGE_NAME="docker.io/myusername/simple-bank-prod:stable"

# Staging  
DOCKER_IMAGE_NAME="docker.io/myusername/simple-bank-staging:develop"

# Development
DOCKER_IMAGE_NAME="docker.io/myusername/simple-bank-dev:latest"
```

## 🔍 Troubleshooting

### Common Issues

1. **Image Not Found**
   - Check Docker Hub username is correct
   - Verify image exists: `docker pull docker.io/username/simple-bank:tag`
   - Check image name in generated deployment

2. **Template Substitution Failed**
   - Ensure `envsubst` is installed
   - Check environment variable is set: `echo $DOCKER_IMAGE_NAME`
   - Verify template syntax: `${VARIABLE_NAME}`

3. **Generated File Issues**
   - Delete generated file: `rm k8s/deployment-generated.yaml`
   - Regenerate: `envsubst < k8s/deployment-template.yaml > k8s/deployment-generated.yaml`

### Debug Commands

```bash
# Check generated deployment
cat k8s/deployment-generated.yaml

# Verify environment variable
echo $DOCKER_IMAGE_NAME

# Test template substitution
envsubst < k8s/deployment-template.yaml

# Check current deployment
kubectl get deployment simple-bank-app -n simple-bank -o yaml
```

## 📝 Best Practices

1. **Use Semantic Versioning**: `v1.0.0`, `v1.1.0`, etc.
2. **Environment-Specific Tags**: `dev`, `staging`, `prod`
3. **Consistent Naming**: Use the same pattern across all environments
4. **Version Control**: Keep generated files out of git (use .gitignore)
5. **Documentation**: Document your image naming convention

## 🔒 Security Considerations

1. **Private Registries**: Use private registries for production
2. **Image Scanning**: Scan images for vulnerabilities
3. **Access Control**: Limit who can push to your Docker registry
4. **Secrets Management**: Store registry credentials securely

## 📚 Related Files

- `k8s/deployment-template.yaml` - Deployment template
- `k8s/image-config.yaml` - ConfigMap configuration
- `scripts/deploy.sh` - Deployment script
- `.github/workflows/ci-cd.yml` - GitHub Actions workflow
- `.gitignore` - Excludes generated files
