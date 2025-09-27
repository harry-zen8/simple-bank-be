# Kubernetes Manifests for Simple Bank Application

This directory contains Kubernetes manifests for deploying the Simple Bank application.

## Structure

- `namespace.yaml` - Creates the simple-bank namespace
- `configmap.yaml` - Application configuration
- `secret.yaml` - Sensitive configuration (passwords, etc.)
- `postgres-deployment.yaml` - PostgreSQL database deployment
- `postgres-service.yaml` - PostgreSQL service
- `deployment.yaml` - Main application deployment
- `service.yaml` - Application service
- `ingress.yaml` - Ingress configuration for external access
- `kustomization.yaml` - Base Kustomization file

## Overlays

- `overlays/production/` - Production environment configuration
- `overlays/staging/` - Staging environment configuration

## Deployment

### Using kubectl directly:
```bash
kubectl apply -f k8s/
```

### Using Kustomize:
```bash
# Base deployment
kubectl apply -k k8s/

# Production deployment
kubectl apply -k k8s/overlays/production/

# Staging deployment
kubectl apply -k k8s/overlays/staging/
```

## Configuration

Before deploying, update the following:

1. **Docker image name** in `deployment.yaml` and kustomization files
2. **Database password** in `secret.yaml` (base64 encoded)
3. **Ingress host** in `ingress.yaml` if using custom domain

## Environment Variables

The application uses the following environment variables:
- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password (from secret)
- `SPRING_JPA_HIBERNATE_DDL_AUTO` - Hibernate DDL mode
- `SPRING_JPA_SHOW_SQL` - Show SQL queries
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` - Actuator endpoints

## Health Checks

The application includes:
- **Liveness probe**: `/actuator/health`
- **Readiness probe**: `/actuator/health`
- **Startup probe**: `/actuator/health`

## Resources

Default resource limits:
- **CPU**: 1000m (1 core)
- **Memory**: 1Gi

Production overlay increases these limits:
- **CPU**: 2000m (2 cores)
- **Memory**: 2Gi
