param(
    [string]$Sha256
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = (Resolve-Path (Join-Path $ScriptDir "..")).Path
$PropertiesFile = Join-Path $RootDir "gradle/wrapper/gradle-wrapper.properties"
$WrapperJar = Join-Path $RootDir "gradle/wrapper/gradle-wrapper.jar"

if (Test-Path $WrapperJar) {
    exit 0
}

if (-not (Test-Path $PropertiesFile)) {
    Write-Error "No se encontró $PropertiesFile"
    exit 1
}

$distributionLine = Select-String -Path $PropertiesFile -Pattern '^distributionUrl=' | Select-Object -First 1
if (-not $distributionLine) {
    Write-Error "distributionUrl no encontrado en $PropertiesFile"
    exit 1
}

$distributionUrl = $distributionLine.Line.Split('=', 2)[1].Trim()
$distributionUrl = $distributionUrl -replace '\\:', ':' -replace '\\', ''

$versionMatch = [regex]::Match($distributionUrl, 'gradle-([^/-]+)-')
if (-not $versionMatch.Success) {
    Write-Error "No se pudo deducir la versión de Gradle a partir de $distributionUrl"
    exit 1
}

$gradleVersion = $versionMatch.Groups[1].Value
$wrapperUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-wrapper.jar"
$shaUrl = "$wrapperUrl.sha256"

$tempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("gradle-wrapper-" + [System.Guid]::NewGuid().ToString())
New-Item -ItemType Directory -Path $tempDir | Out-Null

try {
    if (-not $Sha256) {
        $shaFile = Join-Path $tempDir "gradle-wrapper.jar.sha256"
        Invoke-WebRequest -Uri $shaUrl -OutFile $shaFile -UseBasicParsing
        $Sha256 = (Get-Content $shaFile).Trim()
    }

    if (-not $Sha256) {
        Write-Error "No se pudo obtener el hash sha256 esperado"
        exit 1
    }

    $tmpJar = Join-Path $tempDir "gradle-wrapper.jar"
    Invoke-WebRequest -Uri $wrapperUrl -OutFile $tmpJar -UseBasicParsing

    $calculatedSha = (Get-FileHash -Path $tmpJar -Algorithm SHA256).Hash.ToLower()
    if ($calculatedSha -ne $Sha256.ToLower()) {
        Write-Error "El hash sha256 del wrapper no coincide con el esperado. Esperado: $Sha256 Obtenido: $calculatedSha"
        exit 1
    }

    $destDir = Split-Path -Parent $WrapperJar
    if (-not (Test-Path $destDir)) {
        New-Item -ItemType Directory -Path $destDir | Out-Null
    }

    Move-Item -Path $tmpJar -Destination $WrapperJar -Force
    Write-Host "Gradle Wrapper $gradleVersion descargado correctamente."
}
finally {
    if (Test-Path $tempDir) {
        Remove-Item -Path $tempDir -Recurse -Force
    }
}
