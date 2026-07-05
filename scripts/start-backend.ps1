$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot

$services = @(
    @{ Name = "authservice"; Port = 8080 },
    @{ Name = "accountservice"; Port = 8081 },
    @{ Name = "notificationservice"; Port = 8082 },
    @{ Name = "walletservice"; Port = 8083 },
    @{ Name = "beneficiaryservice"; Port = 8084 },
    @{ Name = "transactionservice"; Port = 8086 },
    @{ Name = "auditservice"; Port = 8087 }
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
