param(
    [string]$Namespace = "ecommerce-dev",
    [string]$ChartPath = ""
)

function Write-Info($message) {
    Write-Host $message
}

if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    throw "kubectl is not installed or not available on PATH."
}

if (-not (Get-Command helm -ErrorAction SilentlyContinue)) {
    throw "helm is not installed or not available on PATH."
}

try {
    kubectl config current-context | Out-Null
}
catch {
    throw "kubectl is not configured. Ensure your context points to minikube before running this script."
}

if (-not $ChartPath) {
    $scriptDir = Split-Path -Path $MyInvocation.MyCommand.Path -Parent
    $ChartPath = Join-Path -Path $scriptDir -ChildPath "..\helm-charts\ecommerce"
    $ChartPath = (Resolve-Path $ChartPath).Path
}

Write-Info "Creating or updating namespace '$Namespace'..."
$namespaceYaml = kubectl create namespace $Namespace --dry-run=client -o yaml
$namespaceYaml | kubectl apply -f - | Out-Null

Write-Info "Deploying Helm release 'ecommerce' into namespace '$Namespace'..."
helm upgrade --install ecommerce $ChartPath --namespace $Namespace | Write-Output

Write-Info "Waiting for 'service-discovery' deployment to become ready..."
if (kubectl rollout status deployment/service-discovery -n $Namespace --timeout=300s) {
    Write-Info "service-discovery deployment is ready."
} else {
    throw "service-discovery deployment did not become ready in the allotted time."
}

Write-Info "Waiting for remaining deployments to become ready..."
$deploymentsJson = kubectl get deployments -n $Namespace -o json | ConvertFrom-Json
$deployments = $deploymentsJson.items | Where-Object { $_.metadata.name -ne "service-discovery" } | ForEach-Object { $_.metadata.name }
foreach ($deployment in $deployments) {
    Write-Info "Waiting for deployment '$deployment'..."
    if (kubectl rollout status deployment/$deployment -n $Namespace --timeout=300s) {
        Write-Info "Deployment '$deployment' is ready."
    } else {
        throw "Deployment '$deployment' did not become ready in the allotted time."
    }
}

Write-Info "Services in namespace '$Namespace':"
kubectl get svc -n $Namespace

Write-Info "Pods in namespace '$Namespace':"
kubectl get pods -n $Namespace

Write-Info "Deployment completed successfully."
