package edu.gpnu.bigdata.benchmark;

import edu.gpnu.bigdata.entity.UserLogRecord;
import edu.gpnu.bigdata.service.StatsService;
import edu.gpnu.bigdata.util.SyntheticDataFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@Fork(1)
@State(Scope.Benchmark)
public class StreamBenchmark {
    private List<UserLogRecord> logs;
    private StatsService statsService;

    @Setup
    public void setup() {
        SyntheticDataFactory factory = new SyntheticDataFactory(50_000);
        logs = java.util.stream.IntStream.rangeClosed(1, 100_000)
                .mapToObj(factory::logAt)
                .toList();
        statsService = new StatsService(logs);
    }

    @Benchmark
    public Map<String, Long> loopCountByEventType() {
        Map<String, Long> counts = new HashMap<>();
        for (UserLogRecord log : logs) {
            counts.merge(log.eventType(), 1L, Long::sum);
        }
        return counts;
    }

    @Benchmark
    public Map<String, Long> streamCountByEventType() {
        return statsService.countByEventType();
    }

    @Benchmark
    public Map<String, Long> parallelStreamCountByEventType() {
        return statsService.countByEventTypeParallel();
    }
}

