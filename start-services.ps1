Write-Host "Iniciando servicios core..."
docker-compose -f core.yml up -d

$zipkinUrl = "http://localhost:9411/health"
$serviceDiscoveryUrl = "http://localhost:8761"
$cloudConfigUrl = "http://localhost:9296/actuator/health"

Write-Host "Esperando a que los servicios core estén listos..."

do {
    $ready = $true

    # Verificar Zipkin
    try {
        $response = Invoke-WebRequest -Uri $zipkinUrl -Method GET -TimeoutSec 5
        if ($response.StatusCode -ne 200) {
            $ready = $false
        }
    } catch {
        $ready = $false
    }

    # Verificar Service Discovery
    try {
        $response = Invoke-WebRequest -Uri $serviceDiscoveryUrl -Method GET -TimeoutSec 5
        if ($response.StatusCode -ne 200) {
            $ready = $false
        }
    } catch {
        $ready = $false
    }

    # Verificar Cloud Config
    try {
        $response = Invoke-WebRequest -Uri $cloudConfigUrl -Method GET -TimeoutSec 5
        if ($response.StatusCode -ne 200) {
            $ready = $false
        }
    } catch {
        $ready = $false
    }

    if (-not $ready) {
        Write-Host "Servicios no listos, esperando 10 segundos..."
        Start-Sleep -Seconds 10
    }
} while (-not $ready)

Write-Host "Servicios core listos. Iniciando servicios principales..."
docker-compose -f core.yml -f compose.yml up -d

Write-Host "Todos los servicios iniciados."