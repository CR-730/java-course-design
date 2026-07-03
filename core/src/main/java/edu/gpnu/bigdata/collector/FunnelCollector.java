package edu.gpnu.bigdata.collector;

import edu.gpnu.bigdata.dto.FunnelStats;
import edu.gpnu.bigdata.entity.UserLogRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class FunnelCollector implements Collector<UserLogRecord, Map<String, Set<Long>>, FunnelStats> {
    @Override
    public Supplier<Map<String, Set<Long>>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<String, Set<Long>>, UserLogRecord> accumulator() {
        return (usersByEvent, log) -> usersByEvent
                .computeIfAbsent(log.eventType(), ignored -> new HashSet<>())
                .add(log.userId());
    }

    @Override
    public BinaryOperator<Map<String, Set<Long>>> combiner() {
        return (left, right) -> {
            right.forEach((eventType, users) -> left
                    .computeIfAbsent(eventType, ignored -> new HashSet<>())
                    .addAll(users));
            return left;
        };
    }

    @Override
    public Function<Map<String, Set<Long>>, FunnelStats> finisher() {
        return usersByEvent -> {
            long view = sizeOf(usersByEvent, "view");
            long cart = sizeOf(usersByEvent, "cart");
            long order = sizeOf(usersByEvent, "order");
            long pay = sizeOf(usersByEvent, "pay");
            return new FunnelStats(
                    view,
                    cart,
                    order,
                    pay,
                    rate(cart, view),
                    rate(order, cart),
                    rate(pay, order)
            );
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }

    private static long sizeOf(Map<String, Set<Long>> usersByEvent, String eventType) {
        return usersByEvent.getOrDefault(eventType, Set.of()).size();
    }

    private static double rate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return Math.round(numerator * 10_000.0 / denominator) / 100.0;
    }
}

