Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Push-Location "$PSScriptRoot\.."
try {
    mvn -pl core "exec:java" "-Dexec.mainClass=edu.gpnu.bigdata.util.ReportGenerator"
} finally {
    Pop-Location
}
