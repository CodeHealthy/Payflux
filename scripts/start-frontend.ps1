$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot "load-env.ps1") -RootPath $root
$frontendPath = Join-Path $root "payflux-frontend"
$frontendPort = if ($env:VITE_DEV_SERVER_PORT) { $env:VITE_DEV_SERVER_PORT } else { "5173" }
$frontendHost = if ($env:VITE_DEV_SERVER_HOST) { $env:VITE_DEV_SERVER_HOST } else { "127.0.0.1" }
$command = "cd /d `"$frontendPath`" && title PayFlux frontend :$frontendPort && npm run dev"

Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $command

Write-Host "Started PayFlux frontend terminal."
Write-Host "Open: http://$frontendHost`:$frontendPort"
