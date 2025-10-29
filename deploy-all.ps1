Write-Host "Desplegando Jenkins en namespace devops..."

# Paso 1: Desplegar en Kubernetes
Write-Host "Desplegando Jenkins en Kubernetes..."
& .\deploy-k8s.ps1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error en el despliegue de Kubernetes"
    exit 1
}
Write-Host "Despliegue en Kubernetes completado."

Write-Host "Para acceder a Jenkins, ejecuta en una terminal separada y mantenla abierta:"
Write-Host "minikube tunnel"
Write-Host ""
Write-Host "Luego, accede a:"
Write-Host "Jenkins: La URL mostrada por minikube tunnel (ej. http://localhost:puerto)"

Write-Host "Despliegue de DevOps tools finalizado exitosamente."
