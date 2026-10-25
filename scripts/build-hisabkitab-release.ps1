param([string]$SigningDirectory = (Join-Path $env:USERPROFILE '.hisabkitab-signing'))
$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path $PSScriptRoot -Parent
$keyFile = Join-Path $SigningDirectory 'hisabkitab-release.p12'
$passwordFile = Join-Path $SigningDirectory 'password.dpapi'
New-Item -ItemType Directory -Force -Path $SigningDirectory | Out-Null
if (!(Test-Path -LiteralPath $keyFile)) {
    if (Test-Path -LiteralPath $passwordFile) { throw 'Existing signing credentials found without a key. Restore the original key rather than replacing it.' }
    $bytes = New-Object byte[] 32
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes); $rng.Dispose()
    $password = [Convert]::ToBase64String($bytes)
    $secure = ConvertTo-SecureString $password -AsPlainText -Force
    ConvertFrom-SecureString $secure | Set-Content -LiteralPath $passwordFile
    $env:HISABKITAB_KEY_PASSWORD = $password
    & keytool -genkeypair -keystore $keyFile -storetype PKCS12 -alias upload -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=HisabKitab' -storepass:env HISABKITAB_KEY_PASSWORD -keypass:env HISABKITAB_KEY_PASSWORD
    if ($LASTEXITCODE -ne 0) { throw 'Signing key generation failed.' }
} else {
    $secure = Get-Content -LiteralPath $passwordFile | ConvertTo-SecureString
    $password = [Net.NetworkCredential]::new('', $secure).Password
}
try {
    $env:KEYSTORE_PATH = $keyFile
    $env:STORE_PASSWORD = $password
    $env:KEY_PASSWORD = $password
    Push-Location $projectDirectory
    & .\gradlew.bat :app:assembleRelease :app:bundleRelease :app:lintRelease --no-configuration-cache --console=plain '-Dorg.gradle.jvmargs=-Xmx1536m'
    if ($LASTEXITCODE -ne 0) { throw 'Release build or lint failed.' }
    New-Item -ItemType Directory -Force releases/HisabKitab | Out-Null
    Copy-Item app/build/outputs/apk/release/app-release.apk releases/HisabKitab/HisabKitab-1.0.0.apk -Force
    Copy-Item app/build/outputs/bundle/release/app-release.aab releases/HisabKitab/HisabKitab-1.0.0.aab -Force
    @('HisabKitab-1.0.0.apk','HisabKitab-1.0.0.aab') | ForEach-Object {
        $hash = (Get-FileHash -Algorithm SHA256 (Join-Path releases/HisabKitab $_)).Hash.ToLower()
        "$hash  $_"
    } | Set-Content releases/HisabKitab/SHA256SUMS.txt
} finally {
    Pop-Location
    Remove-Item Env:KEYSTORE_PATH, Env:STORE_PASSWORD, Env:KEY_PASSWORD, Env:HISABKITAB_KEY_PASSWORD -ErrorAction SilentlyContinue
    $password = $null
}
