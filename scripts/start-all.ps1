$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot

Write-Host "Starting Docker infrastructure..."
Push-Location $root
docker compose up -d
Pop-Location

& (Join-Path $PSScriptRoot "start-backend.ps1")
& (Join-Path $PSScriptRoot "start-frontend.ps1")

Write-Host "PayFlux startup commands launched."
