Write-Host "Aplicando manifiestos de Kubernetes con Kustomize (overlay dev)..."

kustomize build k8s/overlays/dev | kubectl apply -k k8s/base/
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error aplicando overlay dev"
    exit 1
}

Write-Host "Despliegue completado. Verifica con: kubectl get pods -n ecommerce"