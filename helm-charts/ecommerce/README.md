# Ecommerce Microservices Helm Chart

This Helm chart deploys the complete ecommerce microservices application to Kubernetes.

## Architecture

The application consists of the following microservices:

### Infrastructure Services
- **service-discovery** (Eureka) - Port 8761: Service registry
- **cloud-config** - Port 9296: Centralized configuration server

### Gateway
- **api-gateway** - Port 8300: API Gateway for routing requests to microservices

### Business Services
- **user-service** - Port 8700: User management
- **product-service** - Port 8500: Product and category management
- **order-service** - Port 8300: Order and cart management
- **payment-service** - Port 8400: Payment processing
- **shipping-service** - Port 8600: Shipping management
- **favourite-service** - Port 8800: User favourites

## Deployment

### Prerequisites
- Kubernetes cluster (minikube, kind, or production cluster)
- Helm 3.x installed
- kubectl configured to access your cluster

### Install

```bash
# Create namespace
kubectl create namespace ecommerce-dev

# Install the chart
helm install ecommerce ./helm-charts/ecommerce --namespace ecommerce-dev
```

### Upgrade

```bash
helm upgrade ecommerce ./helm-charts/ecommerce --namespace ecommerce-dev
```

### Uninstall

```bash
helm uninstall ecommerce --namespace ecommerce-dev
```

## Accessing Services

### From within the cluster

Services are accessible using their Kubernetes DNS names:
- `http://api-gateway.ecommerce-dev.svc.cluster.local:8300`
- `http://service-discovery.ecommerce-dev.svc.cluster.local:8761`
- etc.

### From outside the cluster

Use port-forwarding for local development:

```bash
# API Gateway
kubectl port-forward -n ecommerce-dev svc/api-gateway 8300:8300

# Service Discovery (Eureka Dashboard)
kubectl port-forward -n ecommerce-dev svc/service-discovery 8761:8761
```

Then access:
- API Gateway: http://localhost:8300
- Eureka Dashboard: http://localhost:8761

## Configuration

All microservices share common configuration through a ConfigMap (`microservices-config`) that includes:
- Spring profiles
- Eureka client configuration
- Service discovery URLs

## Health Checks

All services include readiness and liveness probes that check `/actuator/health` endpoint.

## Docker Images

The chart uses the following Docker images from Docker Hub:
- `selimhorri/api-gateway-ecommerce-boot:0.1.0`
- `selimhorri/service-discovery-ecommerce-boot:0.1.0`
- `selimhorri/cloud-config-ecommerce-boot:0.1.0`
- `selimhorri/user-service-ecommerce-boot:0.1.0`
- `selimhorri/product-service-ecommerce-boot:0.1.0`
- `selimhorri/order-service-ecommerce-boot:0.1.0`
- `selimhorri/payment-service-ecommerce-boot:0.1.0`
- `selimhorri/shipping-service-ecommerce-boot:0.1.0`
- `selimhorri/favourite-service-ecommerce-boot:0.1.0`

## CI/CD Integration

This chart is designed to work with the Jenkins pipeline defined in the root `Jenkinsfile`. The pipeline:
1. Builds all microservices with Maven
2. Runs unit and integration tests
3. Deploys to Kubernetes using this Helm chart
4. Runs E2E tests using Cypress against the deployed services

## Troubleshooting

### Check all pods status
```bash
kubectl get pods -n ecommerce-dev
```

### Check all services
```bash
kubectl get svc -n ecommerce-dev
```

### View logs for a specific service
```bash
kubectl logs -n ecommerce-dev deployment/api-gateway -f
```

### Check if services are registered in Eureka
```bash
kubectl port-forward -n ecommerce-dev svc/service-discovery 8761:8761
# Then open http://localhost:8761 in your browser
```

### Test API Gateway connectivity
```bash
kubectl run -it --rm debug --image=curlimages/curl --restart=Never -n ecommerce-dev -- \
  curl http://api-gateway:8300/actuator/health
```
