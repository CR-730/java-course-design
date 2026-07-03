Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Push-Location "$PSScriptRoot\.."
try {
    mvn -q -pl benchmark -am clean package -DskipTests
    java -jar benchmark\target\benchmarks.jar StreamBenchmark -wi 1 -i 3 -f 1 -bm avgt -tu ms
} finally {
    Pop-Location
}
