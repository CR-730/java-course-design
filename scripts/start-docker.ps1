Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Push-Location "$PSScriptRoot\..\docker"
try {
    docker compose up -d
} finally {
    Pop-Location
}
