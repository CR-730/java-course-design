Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Push-Location "$PSScriptRoot\.."
try {
    mvn -pl core "exec:java" "-Dexec.mainClass=edu.gpnu.bigdata.util.DataGenerator"
} finally {
    Pop-Location
}
