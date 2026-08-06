$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$JarPath = Join-Path $ScriptDir "cicd-cli\target\cicd-cli-1.0-SNAPSHOT.jar"

if (-not (Test-Path $JarPath)) {
    $JarPath = Join-Path $ScriptDir "target\cicd-cli-1.0-SNAPSHOT.jar"
}

if (-not (Test-Path $JarPath)) {
    Write-Error "Error: Executable JAR not found at $JarPath."
    Write-Host "Please build the project first by running: mvn clean package" -ForegroundColor Red
    exit 1
}

java -jar $JarPath $args
