# Public Access Guide for Simple Bank API

This guide explains how to access your Simple Bank API once deployed to Kubernetes on Killercoda.

## 🌐 Public Access Methods

Your application will be accessible through multiple methods:

### 1. Direct NodePort Access (Recommended)
- **URL**: `http://<NODE_IP>:30080`
- **Method**: Direct access to the application service
- **Port**: 30080 (NodePort)

### 2. Ingress Access
- **URL**: `http://<NODE_IP>:30080` (via Ingress Controller)
- **Method**: Access through NGINX Ingress Controller
- **Port**: 30080 (Ingress Controller NodePort)

## 🔍 Finding Your Access URLs

### After GitHub Actions Deployment
The CI/CD pipeline will display the access URLs in the deployment logs. Look for:

```
=== Public Access Information ===
🌐 Direct Application Access (NodePort):
   http://<NODE_IP>:30080

🌐 Application Access via Ingress:
   http://<NODE_IP>:30080

📋 API Endpoints:
   Health Check: http://<NODE_IP>:30080/actuator/health
   Swagger UI: http://<NODE_IP>:30080/swagger-ui/index.html
   API Docs: http://<NODE_IP>:30080/v3/api-docs
```

### Manual Discovery
If you need to find the URLs manually:

```bash
# Get node IP
kubectl get nodes -o wide

# Get service information
kubectl get services -n simple-bank

# Get ingress information
kubectl get ingress -n simple-bank
```

## 📋 Available Endpoints

### Health & Monitoring
- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`

### API Documentation
- **Swagger UI**: `/swagger-ui/index.html`
- **OpenAPI JSON**: `/v3/api-docs`

### Banking API Endpoints
Based on your Spring Boot application, you'll have endpoints like:
- **Customers**: `/api/customers`
- **Accounts**: `/api/accounts`
- **Transactions**: `/api/transactions`
- **Transfers**: `/api/transfers`

## 🧪 Testing Your API

### 1. Health Check
```bash
curl http://<NODE_IP>:30080/actuator/health
```

### 2. Swagger UI
Open in browser: `http://<NODE_IP>:30080/swagger-ui/index.html`

### 3. API Testing with curl
```bash
# Get all customers
curl http://<NODE_IP>:30080/api/customers

# Create a customer
curl -X POST http://<NODE_IP>:30080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com"}'

# Get all accounts
curl http://<NODE_IP>:30080/api/accounts
```

## 🔧 Troubleshooting

### Common Issues

1. **Connection Refused**
   - Check if the application is running: `kubectl get pods -n simple-bank`
   - Verify service is available: `kubectl get services -n simple-bank`
   - Check if NodePort is correct: `kubectl get service simple-bank-service -n simple-bank`

2. **Application Not Ready**
   - Check pod logs: `kubectl logs -f deployment/simple-bank-app -n simple-bank`
   - Check pod status: `kubectl describe pod <pod-name> -n simple-bank`

3. **Database Connection Issues**
   - Verify PostgreSQL is running: `kubectl get pods -n simple-bank | grep postgres`
   - Check database logs: `kubectl logs <postgres-pod-name> -n simple-bank`

### Debug Commands

```bash
# Check all resources
kubectl get all -n simple-bank

# Check ingress controller
kubectl get pods -n ingress-nginx

# Check node information
kubectl get nodes -o wide

# Port forward for local testing
kubectl port-forward service/simple-bank-service 8080:8080 -n simple-bank
```

## 🔒 Security Considerations

### Current Setup
- Application runs as non-root user
- Basic resource limits applied
- Health checks enabled

### Production Recommendations
1. **HTTPS**: Configure SSL/TLS certificates
2. **Authentication**: Implement proper API authentication
3. **Rate Limiting**: Add rate limiting to prevent abuse
4. **Network Policies**: Implement Kubernetes network policies
5. **Monitoring**: Add comprehensive monitoring and alerting

## 📊 Monitoring

### Health Monitoring
```bash
# Check application health
curl http://<NODE_IP>:30080/actuator/health

# Check metrics
curl http://<NODE_IP>:30080/actuator/metrics
```

### Kubernetes Monitoring
```bash
# Check pod status
kubectl get pods -n simple-bank -w

# Check resource usage
kubectl top pods -n simple-bank

# Check events
kubectl get events -n simple-bank --sort-by='.lastTimestamp'
```

## 🚀 Scaling

### Horizontal Scaling
```bash
# Scale application replicas
kubectl scale deployment simple-bank-app --replicas=3 -n simple-bank

# Check scaling status
kubectl get deployment simple-bank-app -n simple-bank
```

### Resource Scaling
Edit the deployment to increase resources:
```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "1000m"
  limits:
    memory: "2Gi"
    cpu: "2000m"
```

## 📝 Notes

- **Killercoda**: The cluster IP might change between sessions
- **NodePort**: Port 30080 is used for both direct access and ingress
- **Persistence**: Database data is stored in emptyDir (not persistent)
- **Load Balancing**: Multiple replicas will be load balanced automatically

## 🆘 Support

If you encounter issues:
1. Check the GitHub Actions logs for deployment status
2. Verify all pods are running: `kubectl get pods -n simple-bank`
3. Check application logs: `kubectl logs -f deployment/simple-bank-app -n simple-bank`
4. Ensure the correct NodePort is being used
5. Verify network connectivity to the cluster
