# GitHub Actions CI/CD Setup Guide

This guide will help you configure GitHub Actions to successfully deploy your Simple Bank application to your minikube cluster via ngrok.

## Prerequisites

- ✅ GitHub repository with the Simple Bank code
- ✅ Docker Hub account
- ✅ ngrok account (free tier works)
- ✅ minikube running locally
- ✅ Application deployed and working locally

## Step 1: Docker Hub Setup

### 1.1 Create Docker Hub Repository

1. Go to [Docker Hub](https://hub.docker.com)
2. Sign in or create an account
3. Click "Create Repository"
4. Repository name: `simple-bank-be`
5. Description: "Simple Bank Backend API"
6. Set to **Public** (or Private if you have a paid plan)
7. Click "Create"

### 1.2 Get Docker Hub Credentials

- **Username**: Your Docker Hub username
- **Password**: Your Docker Hub password (or create an access token)

## Step 2: GitHub Repository Configuration

### 2.1 Add Repository Secrets

Go to your GitHub repository → `Settings` → `Secrets and variables` → `Actions` → `Repository secrets`

Click "New repository secret" and add:

#### Secret: `DOCKER_PASSWORD`
- **Name**: `DOCKER_PASSWORD`
- **Value**: Your Docker Hub password or access token

#### Secret: `KUBECONFIG`
- **Name**: `KUBECONFIG`
- **Value**: Base64 encoded kubeconfig (get this from `./generate-github-kubeconfig.sh`)

### 2.2 Add Repository Variables

Go to `Settings` → `Secrets and variables` → `Actions` → `Variables`

Click "New repository variable" and add:

#### Variable: `DOCKER_USERNAME`
- **Name**: `DOCKER_USERNAME`
- **Value**: Your Docker Hub username

### 2.3 Create Environment

Go to `Settings` → `Environments` → `New environment`

- **Environment name**: `dev`
- **Protection rules**: (Optional)
  - Add required reviewers
  - Restrict to branches: `main`, `develop`

## Step 3: ngrok Setup

### 3.1 Start ngrok

Before each deployment, start ngrok:

```bash
# In your project directory
ngrok start --all --config=ngrok.yml
```

### 3.2 Generate kubeconfig

```bash
# Generate kubeconfig for GitHub Actions
./generate-github-kubeconfig.sh
```

### 3.3 Update GitHub Secret

Copy the base64 encoded kubeconfig from the script output and update the `KUBECONFIG` secret in GitHub.

## Step 4: Test the Pipeline

### 4.1 Trigger Deployment

1. **Push to main or develop branch**:
   ```bash
   git add .
   git commit -m "Test CI/CD pipeline"
   git push origin main
   ```

2. **Or create a pull request** to the main branch

### 4.2 Monitor Deployment

1. Go to your GitHub repository
2. Click "Actions" tab
3. Watch the CI/CD pipeline progress
4. Check logs if any step fails

## Step 5: Verify Deployment

After successful deployment, test your application:

```bash
# Run the test script
./test-ngrok-kubemini.sh
```

## Troubleshooting

### Common Issues

#### 1. Docker Build Fails
- **Cause**: Docker Hub credentials incorrect
- **Solution**: Verify `DOCKER_USERNAME` and `DOCKER_PASSWORD` secrets

#### 2. kubectl Connection Fails
- **Cause**: ngrok not running or kubeconfig outdated
- **Solution**: 
  - Start ngrok: `ngrok start --all --config=ngrok.yml`
  - Regenerate kubeconfig: `./generate-github-kubeconfig.sh`
  - Update GitHub secret

#### 3. Deployment Timeout
- **Cause**: Application takes too long to start
- **Solution**: Check application logs and health checks

#### 4. Health Check Fails
- **Cause**: Application not responding
- **Solution**: Check pod logs and database connectivity

### Debug Commands

```bash
# Check GitHub Actions logs
# Go to Actions tab → Click on failed workflow → Check step logs

# Test locally
./test-ngrok-kubemini.sh

# Check kubectl connectivity
KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info

# Check application status
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get pods -n simple-bank
KUBECONFIG=ngrok-kubeconfig.yaml kubectl logs -f deployment/simple-bank-app -n simple-bank
```

## Important Notes

1. **Keep ngrok running** during deployment
2. **ngrok URLs change** when you restart ngrok - regenerate kubeconfig
3. **Docker images** are pushed to your Docker Hub repository
4. **Deployment** happens to your local minikube via ngrok tunnel
5. **Public access** is available through ngrok HTTP tunnel

## Security Considerations

- This setup is for **development/testing only**
- ngrok tunnels are **publicly accessible**
- Consider using **private repositories** for production
- Use **proper authentication** for production deployments
- **Rotate secrets** regularly

## Next Steps

After successful setup:

1. **Monitor deployments** via GitHub Actions
2. **Test application** using the public ngrok URLs
3. **Set up monitoring** and logging
4. **Configure production environment** when ready
5. **Implement proper security** measures

## Support

If you encounter issues:

1. Check the troubleshooting section above
2. Review GitHub Actions logs
3. Test locally with `./test-ngrok-kubemini.sh`
4. Verify all secrets and variables are set correctly
5. Ensure ngrok is running and accessible
