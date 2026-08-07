# PowerShell installation script for the 'cicd' CLI tool on Windows
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$InstallDir = Join-Path $HOME ".cicd\bin"
$JarName = "cicd-cli.jar"
$ReleaseUrl = "https://github.com/shuvmraj/cicd-cli/releases/download/v1.0.0/cicd-cli.jar"

Write-Host "=== Installing cicd CLI for Windows ===" -ForegroundColor Cyan

# 1. Create installation directory
if (-not (Test-Path $InstallDir)) {
    New-Item -ItemType Directory -Path $InstallDir | Out-Null
}

# 2. Download the JAR file from GitHub Releases
Write-Host "Downloading executable from GitHub Releases..." -ForegroundColor Yellow
$JarPath = Join-Path $InstallDir $JarName

try {
    $WebClient = New-Object System.Net.WebClient
    $WebClient.Headers.Add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    $WebClient.DownloadFile($ReleaseUrl, $JarPath)
    
    # Validate file size (must be greater than 1MB to verify it is not an HTML error/404 page)
    $file = Get-Item $JarPath
    if ($file.Length -lt 1000000) {
        Remove-Item $JarPath -ErrorAction SilentlyContinue
        throw "Downloaded file size is too small ($($file.Length) bytes). The GitHub Release may not be published or does not contain the JAR asset."
    }
} catch {
    Write-Error "Error: Failed to download the JAR from $ReleaseUrl."
    Write-Host "Details: $_" -ForegroundColor Red
    Write-Host "Please check your internet connection or verify the release exists." -ForegroundColor Red
    exit 1
}

# 3. Generate the PowerShell execution wrapper script (cicd.ps1)
$WrapperPathPS = Join-Path $InstallDir "cicd.ps1"
$WrapperContentPS = @"
# PowerShell execution wrapper for cicd CLI
`$JarPath = Join-Path `$Home ".cicd\bin\cicd-cli.jar"
if (-not (Test-Path `$JarPath)) {
    Write-Error "Error: cicd-cli.jar is missing from `$JarPath."
    exit 1
}
java -jar `$JarPath `$args
"@
Set-Content -Path $WrapperPathPS -Value $WrapperContentPS -Encoding UTF8

# 4. Generate the CMD/Batch execution wrapper script (cicd.bat)
$WrapperPathBat = Join-Path $InstallDir "cicd.bat"
$WrapperContentBat = @"
@echo off
SET JAR_PATH=%USERPROFILE%\.cicd\bin\cicd-cli.jar
IF NOT EXIST "%JAR_PATH%" (
    echo Error: cicd-cli.jar is missing from %JAR_PATH%.
    exit /b 1
)
java -jar "%JAR_PATH%" %*
"@
Set-Content -Path $WrapperPathBat -Value $WrapperContentBat -Encoding UTF8

# 5. Add to User Environment Path
Write-Host "Configuring Environment PATH..." -ForegroundColor Yellow
$UserPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($UserPath -notlike "*$InstallDir*") {
    $NewPath = "$UserPath;$InstallDir"
    [Environment]::SetEnvironmentVariable("Path", $NewPath, "User")
    Write-Host "PATH updated successfully! Restart your terminal to apply." -ForegroundColor Green
} else {
    Write-Host "PATH is already configured." -ForegroundColor Green
}

Write-Host "Installation complete! Start a new terminal (CMD or PowerShell) and type: cicd" -ForegroundColor Green
Write-Host "=============================" -ForegroundColor Cyan
