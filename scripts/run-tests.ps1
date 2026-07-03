Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Push-Location "$PSScriptRoot\.."
try {
    mvn clean test
} finally {
    Pop-Location
}
