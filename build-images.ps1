docker build -t ecommerce/service-discovery:latest ./service-discovery
if ($LASTEXITCODE -ne 0) { exit 1 }

docker build -t ecommerce/cloud-config:latest ./cloud-config
if ($LASTEXITCODE -ne 0) { exit 1 }

docker build -t ecommerce/api-gateway:latest ./api-gateway
if ($LASTEXITCODE -ne 0) { exit 1 }

docker build -t ecommerce/proxy-client:latest ./proxy-client
if ($LASTEXITCODE -ne 0) { exit 1 }

docker build -t ecommerce/user-service:latest ./user-service
if ($LASTEXITCODE -ne 0) { exit 1 }

docker build -t ecommerce/product-service:latest ./product-service
if ($LASTEXITCODE -ne 0) { exit 1 }

docker build -t ecommerce/favourite-service:latest ./favourite-service
if ($LASTEXITCODE -ne 0) { exit 1 }

docker build -t ecommerce/order-service:latest ./order-service
if ($LASTEXITCODE -ne 0) { exit 1 }

docker build -t ecommerce/shipping-service:latest ./shipping-service
if ($LASTEXITCODE -ne 0) { exit 1 }

docker build -t ecommerce/payment-service:latest ./payment-service
if ($LASTEXITCODE -ne 0) { exit 1 }