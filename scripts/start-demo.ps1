param([string]$Device = 'dccba126')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$nodeExe = Join-Path $projectRoot 'output/runtime/node_modules/node/bin/node.exe'
$adbExe = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$pgCtl = 'C:/Program Files/PostgreSQL/16/bin/pg_ctl.exe'
$outputDir = Join-Path $projectRoot 'output'
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
function Listening($port) { return [bool](Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue) }
if (-not (Listening 55432)) {
    & $pgCtl -D (Join-Path $projectRoot 'backend/.postgres') -l (Join-Path $projectRoot 'backend/data/postgres.log') -o '-p 55432 -h 127.0.0.1' start
    if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL could not start. See backend/data/postgres.log.' }
}
if (-not (Listening 5001)) {
    $logFile = Join-Path $outputDir 'backend.log'
    if (Test-Path -LiteralPath $logFile) { Copy-Item -LiteralPath $logFile -Destination (Join-Path $outputDir ('backend-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.log')) }
    Start-Process -FilePath $nodeExe -ArgumentList 'dist/index.js' -WorkingDirectory (Join-Path $projectRoot 'backend') -WindowStyle Hidden -RedirectStandardOutput $logFile -RedirectStandardError (Join-Path $outputDir 'backend-errors.log')
}
if (-not (Listening 8081)) {
    $env:REACT_NATIVE_PACKAGER_HOSTNAME = '127.0.0.1'
    Start-Process -FilePath $nodeExe -ArgumentList 'node_modules/expo/bin/cli','start','--lan','--port','8081','--max-workers','2' -WorkingDirectory (Join-Path $projectRoot 'mobile') -WindowStyle Hidden -RedirectStandardOutput (Join-Path $outputDir 'metro.log') -RedirectStandardError (Join-Path $outputDir 'metro-errors.log')
}
$deviceList = & $adbExe devices
if ($deviceList -match ('^' + [regex]::Escape($Device) + '\s+device$')) {
    & $adbExe -s $Device reverse tcp:5001 tcp:5001
    & $adbExe -s $Device reverse tcp:8081 tcp:8081
    Write-Host 'USB forwarding ready. Once Metro is ready, open exp://127.0.0.1:8081 in Expo Go.'
} else { Write-Host 'Phone not connected. Reconnect USB debugging and run this script again.' }
Write-Host 'OTP log: output/backend.log. Database: 127.0.0.1:55432/miniledger.'
Write-Host 'Wait for Metro to finish starting. See docs/VIDEO-DEMO.md for recording commands.'
