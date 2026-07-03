Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Push-Location "$PSScriptRoot\.."
try {
    mvn install -DskipTests
    mvn -pl web "exec:java" "-Dexec.mainClass=edu.gpnu.bigdata.web.ApiServer"
} finally {
    Pop-Location
}
