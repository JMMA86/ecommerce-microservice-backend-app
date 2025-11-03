Write-Host "Verificando herramientas requeridas..." -ForegroundColor Cyan
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Error "kubectl no esta instalado o no esta en el PATH."
    exit 1
}
if (-not (Get-Command helm -ErrorAction SilentlyContinue)) {
    Write-Error "Helm no esta instalado o no esta en el PATH."
    exit 1
}

$namespace = "devops"
$releaseName = "devops-tools"
$chartPath = "./helm-charts/devops"

Write-Host "Verificando si el namespace '$namespace' ya existe..." -ForegroundColor Cyan
$namespaceExists = kubectl get namespace $namespace --no-headers --output=name 2>$null

if (-not $namespaceExists) {
    Write-Host "Creando namespace '$namespace'..." -ForegroundColor Green
    kubectl create namespace $namespace
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Fallo la creacion del namespace '$namespace'."
        exit 1
    }
} else {
    Write-Host "Namespace '$namespace' ya existe." -ForegroundColor Yellow
}

Write-Host "Aplicando anotaciones y etiquetas del namespace para Helm..." -ForegroundColor Green
kubectl annotate namespace $namespace meta.helm.sh/release-name=$releaseName meta.helm.sh/release-namespace=$namespace --overwrite
kubectl label namespace $namespace app.kubernetes.io/managed-by=Helm --overwrite

if ($LASTEXITCODE -ne 0) {
    Write-Error "Fallo la aplicacion de anotaciones o etiquetas al namespace."
    exit 1
}

Write-Host "Desplegando Helm release '$releaseName' en namespace '$namespace'..." -ForegroundColor Green
helm upgrade --install $releaseName $chartPath --namespace $namespace

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error desplegando con Helm." -ForegroundColor Red
    exit 1
}

Write-Host "Despliegue completado exitosamente." -ForegroundColor Green

Write-Host ""
Write-Host "Para acceder a Jenkins:" -ForegroundColor Cyan
Write-Host "1. Ejecuta en una terminal separada y mantenla abierta:" -ForegroundColor Gray
Write-Host "   minikube tunnel" -ForegroundColor White
Write-Host "2. Una vez activo el tunel, obten la URL de Jenkins con:" -ForegroundColor Gray
Write-Host "   kubectl get svc -n $namespace" -ForegroundColor White
Write-Host "   (Busca la IP externa o puerto asignado)"
Write-Host ""
Write-Host "Despliegue de DevOps tools finalizado exitosamente." -ForegroundColor Green
