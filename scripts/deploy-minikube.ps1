param(
    [string]$Namespace = "ecommerce-dev",
    [string]$ChartPath = "",
    [switch]$BuildImages = $true
)

function Write-Info($message) {
    Write-Host $message -ForegroundColor Cyan
}

function Write-Success($message) {
    Write-Host $message -ForegroundColor Green
}

function Write-Error($message) {
    Write-Host $message -ForegroundColor Red
}

if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    throw "kubectl is not installed or not available on PATH."
}

if (-not (Get-Command helm -ErrorAction SilentlyContinue)) {
    throw "helm is not installed or not available on PATH."
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "docker is not installed or not available on PATH."
}

try {
    kubectl config current-context | Out-Null
}
catch {
    throw "kubectl is not configured. Ensure your context points to minikube before running this script."
}

$scriptDir = Split-Path -Path $MyInvocation.MyCommand.Path -Parent
$projectRoot = Split-Path -Path $scriptDir -Parent

if (-not $ChartPath) {
    $ChartPath = Join-Path -Path $projectRoot -ChildPath "helm-charts\ecommerce"
    $ChartPath = (Resolve-Path $ChartPath).Path
}

# Build images locally in Minikube
if ($BuildImages) {
    $services = @(
        "service-discovery",
        "cloud-config", 
        "api-gateway",
        "user-service",
        "product-service",
        "order-service",
        "payment-service",
        "shipping-service",
        "favourite-service"
    )
    
    Write-Info "Building Maven projects..."
    foreach ($service in $services) {
        $servicePath = Join-Path -Path $projectRoot -ChildPath $service
        
        if (Test-Path $servicePath) {
            Write-Info "Building Maven project for $service..."
            try {
                Set-Location $servicePath
                
                # Build with Maven using the wrapper
                if (Test-Path ".\mvnw.cmd") {
                    .\mvnw.cmd clean package -DskipTests
                } else {
                    mvn clean package -DskipTests
                }
                
                if ($LASTEXITCODE -eq 0) {
                    Write-Success "Successfully built Maven project for ${service}"
                } else {
                    Write-Error "Failed to build Maven project for ${service}"
                    throw "Maven build failed for ${service}"
                }
            }
            catch {
                Write-Error "Error building Maven project for ${service}: $_"
                throw
            }
        } else {
            Write-Error "Service directory not found: $servicePath"
        }
    }
    
    Set-Location $projectRoot
    Write-Success "All Maven projects built successfully!"
    
    Write-Info "Configuring Docker to use Minikube's Docker daemon..."
    & minikube -p minikube docker-env --shell powershell | Invoke-Expression
    
    Write-Info "Building Docker images inside Minikube..."
    foreach ($service in $services) {
        $servicePath = Join-Path -Path $projectRoot -ChildPath $service
        
        if (Test-Path $servicePath) {
            Write-Info "Building Docker image for $service..."
            try {
                Set-Location $servicePath
                docker build -t "${service}:latest" .
                if ($LASTEXITCODE -eq 0) {
                    Write-Success "Successfully built Docker image ${service}:latest"
                } else {
                    Write-Error "Failed to build Docker image ${service}"
                    throw "Docker build failed for ${service}"
                }
            }
            catch {
                Write-Error "Error building Docker image for ${service}: $_"
                throw
            }
        }
    }
    
    Set-Location $projectRoot
    Write-Success "All Docker images built successfully!"
}

Write-Info "Creating or updating namespace '$Namespace'..."
$namespaceYaml = kubectl create namespace $Namespace --dry-run=client -o yaml
$namespaceYaml | kubectl apply -f - | Out-Null

Write-Info "Deploying Helm release 'ecommerce' into namespace '$Namespace'..."
helm upgrade --install ecommerce $ChartPath --namespace $Namespace | Write-Output

Write-Info "Waiting for 'service-discovery' deployment to become ready..."
if (kubectl rollout status deployment/service-discovery -n $Namespace --timeout=600s) {
    Write-Success "service-discovery deployment is ready."
} else {
    throw "service-discovery deployment did not become ready in the allotted time."
}

Write-Info "Waiting for remaining deployments to become ready..."
$deploymentsJson = kubectl get deployments -n $Namespace -o json | ConvertFrom-Json
$deployments = $deploymentsJson.items | Where-Object { $_.metadata.name -ne "service-discovery" } | ForEach-Object { $_.metadata.name }
foreach ($deployment in $deployments) {
    Write-Info "Waiting for deployment '$deployment'..."
    if (kubectl rollout status deployment/$deployment -n $Namespace --timeout=300s) {
        Write-Success "Deployment '$deployment' is ready."
    } else {
        Write-Error "Deployment '$deployment' did not become ready in the allotted time."
    }
}

Write-Info "Services in namespace '$Namespace':"
kubectl get svc -n $Namespace

Write-Info "Pods in namespace '$Namespace':"
kubectl get pods -n $Namespace

Write-Success "Deployment completed successfully!"