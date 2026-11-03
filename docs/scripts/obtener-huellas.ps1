# Obtiene el SHA-1, el SHA-256 y el HASH DE CLAVE de Facebook de la llave de depuracion de ESTA PC.
#
# Uso (PowerShell, desde la carpeta del proyecto):
#   powershell -ExecutionPolicy Bypass -File docs\scripts\obtener-huellas.ps1
#
# Opcional: otra llave
#   powershell -ExecutionPolicy Bypass -File docs\scripts\obtener-huellas.ps1 -Keystore "C:\ruta\mi.keystore"

param(
    [string]$Keystore = (Join-Path $env:USERPROFILE ".android\debug.keystore"),
    [string]$Alias = "androiddebugkey",
    [string]$Password = "android"
)

function Buscar-Keytool {
    $candidatos = @()
    if ($env:JAVA_HOME) { $candidatos += (Join-Path $env:JAVA_HOME "bin\keytool.exe") }
    $enPath = Get-Command keytool -ErrorAction SilentlyContinue
    if ($enPath) { $candidatos += $enPath.Source }
    $candidatos += "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"
    $candidatos += (Join-Path $env:LOCALAPPDATA "Programs\Android Studio\jbr\bin\keytool.exe")
    foreach ($patron in @("C:\Program Files\Eclipse Adoptium\*\bin\keytool.exe",
                          "C:\Program Files\Java\*\bin\keytool.exe",
                          "C:\Program Files\Microsoft\*\bin\keytool.exe")) {
        $candidatos += (Get-ChildItem $patron -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName })
    }
    foreach ($c in $candidatos) { if ($c -and (Test-Path $c)) { return $c } }
    return $null
}

Write-Host ""
$keytool = Buscar-Keytool
if (-not $keytool) {
    Write-Host "No encontre keytool. Instala Android Studio (trae el JDK) o define JAVA_HOME." -ForegroundColor Red
    exit 1
}
Write-Host "keytool : $keytool"
Write-Host "llave   : $Keystore"

if (-not (Test-Path $Keystore)) {
    Write-Host ""
    Write-Host "No existe la llave de depuracion en esta PC." -ForegroundColor Yellow
    Write-Host "Se crea sola la primera vez que ejecutas la app en Android Studio (boton Run)."
    Write-Host "Tambien puedes crearla ahora con:"
    Write-Host "  & `"$keytool`" -genkey -v -keystore `"$Keystore`" -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname `"CN=Android Debug,O=Android,C=US`""
    exit 1
}

$salida = & $keytool -list -v -keystore $Keystore -alias $Alias -storepass $Password -keypass $Password 2>&1 | Out-String
$sha1 = $null; $sha256 = $null
if ($salida -match "SHA-?1:\s*([0-9A-Fa-f:]{59})") { $sha1 = $Matches[1] }
if ($salida -match "SHA-?256:\s*([0-9A-Fa-f:]{95})") { $sha256 = $Matches[1] }

if (-not $sha1) {
    Write-Host ""
    Write-Host "No pude leer el SHA-1. Salida de keytool:" -ForegroundColor Red
    Write-Host $salida
    exit 1
}

# El hash de clave de Facebook es el SHA-1 (20 bytes) codificado en base64.
$bytes = [byte[]] ($sha1.Split(":") | ForEach-Object { [Convert]::ToByte($_, 16) })
$hash = [Convert]::ToBase64String($bytes)

Write-Host ""
Write-Host "================ COPIA ESTOS VALORES ================" -ForegroundColor Green
Write-Host "SHA-1 (Firebase -> Add fingerprint):"
Write-Host "  $sha1"
Write-Host ""
Write-Host "HASH DE CLAVE (Meta -> Hashes de clave):"
Write-Host "  $hash"
if ($sha256) {
    Write-Host ""
    Write-Host "SHA-256 (opcional, Firebase tambien lo acepta):"
    Write-Host "  $sha256"
}
Write-Host "=====================================================" -ForegroundColor Green
Write-Host ""
