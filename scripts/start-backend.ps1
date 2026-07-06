$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot "load-env.ps1") -RootPath $root

$services = @(
    @{ Name = "gatewayservice"; Port = if ($env:GATEWAYSERVICE_PORT) { $env:GATEWAYSERVICE_PORT } else { "8088" } },
    @{ Name = "authservice"; Port = if ($env:AUTHSERVICE_PORT) { $env:AUTHSERVICE_PORT } else { "8080" } },
    @{ Name = "accountservice"; Port = if ($env:ACCOUNTSERVICE_PORT) { $env:ACCOUNTSERVICE_PORT } else { "8081" } },
    @{ Name = "notificationservice"; Port = if ($env:NOTIFICATIONSERVICE_PORT) { $env:NOTIFICATIONSERVICE_PORT } else { "8082" } },
    @{ Name = "walletservice"; Port = if ($env:WALLETSERVICE_PORT) { $env:WALLETSERVICE_PORT } else { "8083" } },
    @{ Name = "beneficiaryservice"; Port = if ($env:BENEFICIARYSERVICE_PORT) { $env:BENEFICIARYSERVICE_PORT } else { "8084" } },
    @{ Name = "transactionservice"; Port = if ($env:TRANSACTIONSERVICE_PORT) { $env:TRANSACTIONSERVICE_PORT } else { "8086" } },
    @{ Name = "auditservice"; Port = if ($env:AUDITSERVICE_PORT) { $env:AUDITSERVICE_PORT } else { "8087" } }
)

foreach ($service in $services) {
    $servicePath = Join-Path $root $service.Name
    $title = "PayFlux $($service.Name) :$($service.Port)"
    $command = "cd /d `"$servicePath`" && title $title && .\mvnw.cmd spring-boot:run"

    Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $command
    Start-Sleep -Milliseconds 600
}

Write-Host "Started PayFlux backend service terminals."
Write-Host "Make sure Docker is running with: docker compose up -d"
