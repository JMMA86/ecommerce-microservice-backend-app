# E2E Tests with Cypress

This directory contains end-to-end tests for the microservices using Cypress.

## Prerequisites

- Node.js installed
- All microservices running locally or in Kubernetes

## Installation

```bash
npm install
```

## Running Tests

### Local Development

To run all tests in headless mode against localhost:

```bash
npm test
```

To open Cypress GUI:

```bash
npm run cypress:open
```

### Against Kubernetes Cluster

To run tests against a Kubernetes cluster, set the environment variables:

```bash
export CYPRESS_BASE_URL=http://api-gateway.ecommerce-dev.svc.cluster.local:8300
export CYPRESS_EUREKA_URL=http://service-discovery.ecommerce-dev.svc.cluster.local:8761
npm test
```

Or for local Kubernetes (minikube/kind) with port-forwarding:

```bash
# Port forward the API Gateway
kubectl port-forward -n ecommerce-dev svc/api-gateway 8300:8300

# Then run tests
npm test
```

## Configuration

The tests use environment variables for configuration:

- `CYPRESS_BASE_URL`: Base URL for the API Gateway (default: `http://localhost:8300`)
- `CYPRESS_EUREKA_URL`: URL for Eureka Service Discovery (default: `http://localhost:8761`)

These variables are automatically set in the Jenkins CI/CD pipeline to use Kubernetes service names.

## CI/CD Integration

These tests are integrated into the Jenkins pipeline. In the CI environment:
- Xvfb is installed to run Cypress in headless mode on Linux
- Environment variables are set to use Kubernetes service names (e.g., `api-gateway.ecommerce-dev.svc.cluster.local`)
- Tests run after the services are deployed to the `ecommerce-dev` namespace

## Test Structure

Each microservice has its own test file with 5 e2e tests validating complete user flows:

- **user-service.cy.js**: User management flows (CRUD operations)
- **product-service.cy.js**: Product and category management
- **order-service.cy.js**: Order and cart management
- **payment-service.cy.js**: Payment processing
- **shipping-service.cy.js**: Order item shipping
- **favourite-service.cy.js**: User favourites
- **service-discovery.cy.js**: Service registry health checks

## Architecture

All microservices (except service-discovery) are accessed through the API Gateway:
- Local: `http://localhost:8300/{service-name}`
- Kubernetes: `http://api-gateway.ecommerce-dev.svc.cluster.local:8300/{service-name}`

The Service Discovery (Eureka) is accessed directly:
- Local: `http://localhost:8761`
- Kubernetes: `http://service-discovery.ecommerce-dev.svc.cluster.local:8761`

## Notes

- Tests assume some initial data exists (e.g., users with ID 1, products with ID 1)
- Tests create, read, update, and delete entities
- Ensure services are running and healthy before executing tests
- In CI/CD, the pipeline waits 60 seconds after deployment before running tests to allow services to stabilize
