# JMH Benchmark Report

## Purpose

Compare three implementations for counting user behavior events on 100,000 in-memory log records:

- `loopCountByEventType`: plain `for` loop with `Map.merge`.
- `streamCountByEventType`: sequential Stream API implementation.
- `parallelStreamCountByEventType`: `parallelStream` implementation.

## Command

```powershell
java -jar benchmark\target\benchmarks.jar StreamBenchmark -wi 1 -i 3 -f 1 -bm avgt -tu ms
```

Raw output is saved in `docs/jmh-output.txt`.

## Environment

- JMH: 1.37
- JVM: JDK 25.0.2, Java HotSpot 64-Bit Server VM
- Benchmark mode: average time
- Warmup: 1 iteration
- Measurement: 3 iterations
- Fork: 1
- Dataset: 100,000 synthetic `UserLogRecord` records

## Result

| Benchmark | Score | Error | Unit |
| --- | ---: | ---: | --- |
| `loopCountByEventType` | 2.548 | 0.374 | ms/op |
| `streamCountByEventType` | 2.606 | 0.388 | ms/op |
| `parallelStreamCountByEventType` | 5.527 | 0.399 | ms/op |

## Analysis

The plain loop and sequential Stream implementations are close in this benchmark. The sequential Stream version is slightly slower than the loop, but it keeps the statistics logic concise and readable.

The `parallelStream` version is slower on this dataset. The task only groups 100,000 records by four event types, so the parallel execution overhead is larger than the benefit from splitting the work. This result is acceptable for the course requirement because the project demonstrates correct `parallelStream` usage and measures it with JMH instead of assuming it is faster.

## Related Code

- `core/src/main/java/edu/gpnu/bigdata/service/StatsService.java`
- `core/src/main/java/edu/gpnu/bigdata/collector/FunnelCollector.java`
- `benchmark/src/main/java/edu/gpnu/bigdata/benchmark/StreamBenchmark.java`
