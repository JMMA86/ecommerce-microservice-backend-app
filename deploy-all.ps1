Write-Host "Iniciando flujo completo de despliegue..."

# Paso 1: Construir el proyecto con Maven
Write-Host "Construyendo proyecto con Maven..."
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error en la construcción con Maven"
    exit 1
}
Write-Host "Construcción Maven completada."

# Paso 2: Construir imágenes Docker
Write-Host "Construyendo imagenes Docker..."
& .\build-images.ps1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error en la construcción de imagenes Docker"
    exit 1
}
Write-Host "Imagenes Docker construidas."

# Paso 3: Desplegar en Kubernetes
Write-Host "Desplegando en Kubernetes..."
& .\deploy-k8s.ps1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error en el despliegue de Kubernetes"
    exit 1
}
Write-Host "Despliegue en Kubernetes completado."

Write-Host "Para acceder a la aplicacion, ejecuta uno de los siguientes comandos en una terminal con privilegios de administrador:"
Write-Host "kubectl port-forward svc/api-gateway 8081:8080 -n ecommerce"
Write-Host "kubectl port-forward svc/jenkins 8080:8080 -n ecommerce"
Write-Host "Luego, accede a http://localhost:8081 (para api-gateway) o http://localhost:8080 (para Jenkins)."

Write-Host "Flujo completo finalizado exitosamente."
