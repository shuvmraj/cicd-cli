# PowerShell installation script for the 'cicd' CLI tool on Windows

$InstallDir = Join-Path $HOME ".cicd\bin"
$JarName = "cicd-cli.jar"
$BinName = "cicd.ps1"
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
    Invoke-WebRequest -Uri $ReleaseUrl -OutFile $JarPath -UseBasicParsing
} catch {
    Write-Error "Error: Failed to download the JAR from $ReleaseUrl."
    Write-Host "Please check your internet connection or verify the release exists." -ForegroundColor Red
    exit 1
}

# 3. Generate the PowerShell wrapper script
$WrapperPath = Join-Path $InstallDir $BinName
$WrapperContent = @"
# PowerShell execution wrapper for cicd CLI
`$JarPath = Join-Path `$Home ".cicd\bin\cicd-cli.jar"
if (-not (Test-Path `$JarPath)) {
    Write-Error "Error: cicd-cli.jar is missing from `$JarPath."
    exit 1
}
java -jar `$JarPath `$args
"@

Set-Content -Path $WrapperPath -Value $WrapperContent -Encoding UTF8

# 4. Add to User Environment Path
Write-Host "Configuring Environment PATH..." -ForegroundColor Yellow
$UserPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($UserPath -notlike "*$InstallDir*") {
    $NewPath = "$UserPath;$InstallDir"
    [Environment]::SetEnvironmentVariable("Path", $NewPath, "User")
    Write-Host "PATH updated successfully! Restart your terminal to apply." -ForegroundColor Green
} else {
    Write-Host "PATH is already configured." -ForegroundColor Green
}

Write-Host "Installation complete! Start a new PowerShell terminal and type: cicd" -ForegroundColor Green
Write-Host "=============================" -ForegroundColor Cyan
