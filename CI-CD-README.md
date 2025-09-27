# CI/CD Pipeline for Simple Bank Application

This document describes the CI/CD pipeline setup for the Simple Bank Spring Boot application using GitHub Actions and Kubernetes.

## Overview

The CI/CD pipeline automatically:
1. **Tests** the application with PostgreSQL database
2. **Builds** Docker images and pushes to Docker Hub
3. **Deploys** to Kubernetes cluster on Killercoda

## Prerequisites

### GitHub Secrets
Configure the following secrets in your GitHub repository:

1. **DOCKER_USERNAME** - Your Docker Hub username
2. **DOCKER_PASSWORD** - Your Docker Hub Personal Access Token (PAT)
3. **KUBECONFIG** - Base64-encoded kubeconfig from Killercoda

### Killercoda Setup
1. Access your Kubernetes playground at: https://killercoda.com/playgrounds/course/kubernetes-playgrounds/one-node
2. Get your kubeconfig and encode it:
   ```bash
   cat ~/.kube/config | base64 -w 0
   ```
3. Add the encoded kubeconfig as `KUBECONFIG` secret in GitHub

## Pipeline Stages

### 1. Test Stage
- Runs on every push and pull request
- Sets up PostgreSQL database service
- Executes Maven tests with database integration
- Generates test reports and coverage
- Uploads test artifacts

### 2. Build and Push Stage
- Runs only on main and develop branches
- Builds Docker image using multi-stage build
- Pushes to Docker Hub with appropriate tags
- Uses Docker Buildx for multi-platform builds
- Implements layer caching for faster builds

### 3. Deploy Stage
- Deploys to Kubernetes cluster
- Creates namespace and applies all manifests
- Waits for deployment to be ready
- Performs health checks
- Reports deployment status

## Kubernetes Manifests

The `k8s/` directory contains:

- **Base manifests**: Core application and database
- **Overlays**: Environment-specific configurations
  - `production/`: Production environment with higher resources
  - `staging/`: Staging environment with lower resources

### Key Components

1. **Namespace**: `simple-bank`
2. **Database**: PostgreSQL with persistent storage
3. **Application**: Spring Boot app with health checks
4. **Service**: LoadBalancer for external access
5. **Ingress**: Optional ingress for custom domains

## Configuration

### Environment Variables
The application uses ConfigMaps and Secrets for configuration:

```yaml
# ConfigMap
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-service:5432/banking_db
SPRING_DATASOURCE_USERNAME: user
SPRING_JPA_HIBERNATE_DDL_AUTO: none
SPRING_JPA_SHOW_SQL: true

# Secret
SPRING_DATASOURCE_PASSWORD: <base64-encoded-password>
```

### Resource Limits
- **Base**: 1 CPU, 1Gi memory
- **Production**: 2 CPU, 2Gi memory
- **Staging**: 0.5 CPU, 512Mi memory

## Deployment

### Automatic Deployment
The pipeline automatically deploys on:
- Push to `main` branch → Production
- Push to `develop` branch → Staging

### Manual Deployment
Use the provided deployment script:

```bash
# Deploy to base environment
./scripts/deploy.sh

# Deploy to production
./scripts/deploy.sh production stable

# Deploy to staging
./scripts/deploy.sh staging develop
```

### Using kubectl directly:

```bash
# Base deployment
kubectl apply -k k8s/

# Production deployment
kubectl apply -k k8s/overlays/production/

# Staging deployment
kubectl apply -k k8s/overlays/staging/
```

## Monitoring and Health Checks

### Health Endpoints
- **Liveness**: `/actuator/health`
- **Readiness**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`

### Monitoring Commands
```bash
# Check pod status
kubectl get pods -n simple-bank

# View logs
kubectl logs -f deployment/simple-bank-app -n simple-bank

# Check service
kubectl get services -n simple-bank

# Port forward for local access
kubectl port-forward service/simple-bank-service 8080:8080 -n simple-bank
```

## Troubleshooting

### Common Issues

1. **Image Pull Errors**
   - Check Docker Hub credentials
   - Verify image name and tag

2. **Database Connection Issues**
   - Ensure PostgreSQL is running
   - Check database credentials in secrets

3. **Health Check Failures**
   - Verify application is starting correctly
   - Check resource limits
   - Review application logs

4. **Deployment Timeout**
   - Check cluster resources
   - Verify image size and startup time
   - Review resource requests/limits

### Debug Commands
```bash
# Describe deployment
kubectl describe deployment simple-bank-app -n simple-bank

# Check events
kubectl get events -n simple-bank --sort-by='.lastTimestamp'

# Debug pod
kubectl exec -it <pod-name> -n simple-bank -- /bin/bash
```

## Security Considerations

1. **Non-root User**: Application runs as non-root user
2. **Resource Limits**: Prevents resource exhaustion
3. **Health Checks**: Ensures application stability
4. **Secrets Management**: Sensitive data in Kubernetes secrets
5. **Network Policies**: Consider implementing for production

## Customization

### Adding New Environments
1. Create new overlay directory: `k8s/overlays/<environment>/`
2. Add kustomization.yaml with specific configurations
3. Update GitHub Actions workflow if needed

### Modifying Resources
Edit the appropriate deployment patch file in overlays:
```yaml
# k8s/overlays/production/deployment-patch.yaml
spec:
  template:
    spec:
      containers:
      - name: simple-bank
        resources:
          requests:
            memory: "2Gi"
            cpu: "2000m"
          limits:
            memory: "4Gi"
            cpu: "4000m"
```

## Support

For issues or questions:
1. Check GitHub Actions logs
2. Review Kubernetes events and logs
3. Verify configuration and secrets
4. Check resource availability in cluster
