$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$frontendPath = Join-Path $root "payflux-frontend"
$command = "cd /d `"$frontendPath`" && title PayFlux frontend :5173 && npm run dev -- --host 127.0.0.1"

Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $command

Write-Host "Started PayFlux frontend terminal."
Write-Host "Open: http://127.0.0.1:5173"
