# Testing kubemini (minikube) via ngrok

This guide explains how to test if your kubemini (minikube) exposed via ngrok is working properly.

## 🚀 Quick Start

### 1. Start ngrok tunnel
```bash
# In one terminal, start ngrok
ngrok tcp 8443
```

### 2. Generate kubeconfig for ngrok
```bash
# Use the ngrok TCP URL (e.g., tcp://0.tcp.ngrok.io:12345)
./create-ngrok-kubeconfig.sh tcp://0.tcp.ngrok.io:12345
```

### 3. Run comprehensive tests
```bash
# Test everything
./test-ngrok-kubemini.sh tcp://0.tcp.ngrok.io:12345
```

## 🧪 Manual Testing Steps

### Step 1: Test kubectl Connection
```bash
# Test basic connectivity
KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info

# Check nodes
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get nodes

# Check namespaces
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get namespaces
```

### Step 2: Check Application Status
```bash
# Check if application is deployed
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get pods -n simple-bank

# Check services
KUBECONFIG=ngrok-kubemini.yaml kubectl get services -n simple-bank

# Check deployments
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get deployments -n simple-bank
```

### Step 3: Test Application Endpoints
```bash
# Get node IP
NODE_IP=$(KUBECONFIG=ngrok-kubeconfig.yaml kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')

# Get service port
NODEPORT=$(KUBECONFIG=ngrok-kubeconfig.yaml kubectl get service simple-bank-service -n simple-bank -o jsonpath='{.spec.ports[0].nodePort}')

# Test health endpoint
curl http://$NODE_IP:$NODEPORT/actuator/health

# Test Swagger UI
curl http://$NODE_IP:$NODEPORT/swagger-ui/index.html
```

### Step 4: Test Port Forwarding
```bash
# Port forward to local machine
KUBECONFIG=ngrok-kubeconfig.yaml kubectl port-forward service/simple-bank-service 8080:8080 -n simple-bank

# In another terminal, test local access
curl http://localhost:8080/actuator/health
```

## 🔍 Troubleshooting

### Common Issues

#### 1. kubectl Connection Failed
```bash
# Check if ngrok is running
curl -s http://localhost:4040/api/tunnels | jq '.tunnels[0].public_url'

# Verify kubeconfig
cat ngrok-kubeconfig.yaml

# Test with verbose output
KUBECONFIG=ngrok-kubeconfig.yaml kubectl cluster-info --v=6
```

#### 2. Application Not Accessible
```bash
# Check pod status
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get pods -n simple-bank -o wide

# Check pod logs
KUBECONFIG=ngrok-kubeconfig.yaml kubectl logs -f deployment/simple-bank-app -n simple-bank

# Check service endpoints
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get endpoints -n simple-bank
```

#### 3. Network Connectivity Issues
```bash
# Test node connectivity
NODE_IP=$(KUBECONFIG=ngrok-kubeconfig.yaml kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
ping $NODE_IP

# Test port connectivity
telnet $NODE_IP 30080
```

## 📋 Expected Results

### Successful Test Output
```
🧪 Testing kubemini (minikube) exposed via ngrok
==================================================
Using ngrok URL: tcp://0.tcp.ngrok.io:12345

1️⃣ Testing kubectl connection through ngrok...
----------------------------------------
✅ kubectl connection successful
Kubernetes control plane is running at https://0.tcp.ngrok.io:12345

2️⃣ Checking cluster and node status...
----------------------------------------
Nodes:
NAME       STATUS   ROLES           AGE   VERSION   INTERNAL-IP   EXTERNAL-IP
minikube   Ready    control-plane   1h    v1.28.0   192.168.49.2   <none>

3️⃣ Checking application deployment...
----------------------------------------
✅ Namespace 'simple-bank' exists
Deployments:
NAME              READY   UP-TO-DATE   AVAILABLE   AGE
simple-bank-app   1/1     1            1           1h

4️⃣ Testing application endpoints...
----------------------------------------
Node IP: 192.168.49.2
Application NodePort: 30080
✅ Health endpoint accessible
✅ Swagger UI accessible
✅ API docs accessible

5️⃣ Testing port forwarding...
----------------------------------------
✅ Port forwarding works
   Local URL: http://localhost:8080

6️⃣ Summary and Access Information
==================================================
🎉 kubemini via ngrok is working!

📋 Access URLs:
   Direct Access: http://192.168.49.2:30080
   Health Check: http://192.168.49.2:30080/actuator/health
   Swagger UI: http://192.168.49.2:30080/swagger-ui/index.html
   API Docs: http://192.168.49.2:30080/v3/api-docs
```

## 🔧 Advanced Testing

### Load Testing
```bash
# Test with multiple concurrent requests
for i in {1..10}; do
  curl -s http://$NODE_IP:$NODEPORT/actuator/health &
done
wait
```

### API Testing
```bash
# Test customer creation
curl -X POST http://$NODE_IP:$NODEPORT/api/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com"}'

# Test account creation
curl -X POST http://$NODE_IP:$NODEPORT/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"accountType":"CHECKING","initialBalance":1000.00}'
```

### Monitoring
```bash
# Watch pod status
KUBECONFIG=ngrok-kubeconfig.yaml kubectl get pods -n simple-bank -w

# Monitor logs
KUBECONFIG=ngrok-kubeconfig.yaml kubectl logs -f deployment/simple-bank-app -n simple-bank

# Check resource usage
KUBECONFIG=ngrok-kubeconfig.yaml kubectl top pods -n simple-bank
```

## 🚨 Important Notes

1. **Keep ngrok running**: The tunnel must stay active for kubectl to work
2. **URL changes**: If you restart ngrok, you'll need to regenerate the kubeconfig
3. **Security**: The ngrok tunnel is publicly accessible - use only for testing
4. **Performance**: Network latency will be higher through ngrok
5. **Timeouts**: Some operations may timeout due to network latency

## 🎯 Next Steps

Once testing is successful:
1. Use the base64 encoded kubeconfig for GitHub Actions
2. Set up proper CI/CD with the ngrok tunnel
3. Consider using a more permanent solution for production
4. Implement proper security measures
