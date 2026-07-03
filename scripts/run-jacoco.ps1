Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Push-Location "$PSScriptRoot\.."
try {
    Remove-Item -LiteralPath "$env:USERPROFILE\java-course-design-core-jacoco.exec" -ErrorAction SilentlyContinue
    mvn -pl core clean verify
} finally {
    Pop-Location
}
