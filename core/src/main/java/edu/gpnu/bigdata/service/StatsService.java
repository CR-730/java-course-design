package edu.gpnu.bigdata.service;

import edu.gpnu.bigdata.dto.FunnelStats;
import edu.gpnu.bigdata.entity.UserLogRecord;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StatsService {
    private final List<UserLogRecord> logs;

    public StatsService(List<UserLogRecord> logs) {
        this.logs = List.copyOf(logs);
    }

    public Map<String, Long> countByEventType() {
        return logs.stream()
                .collect(Collectors.groupingBy(
                        UserLogRecord::eventType,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> countByChannel() {
        return logs.stream()
                .collect(Collectors.groupingBy(
                        UserLogRecord::channel,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public Map<LocalDate, Long> dailyPv() {
        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.eventTime().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public Map<LocalDate, Long> dailyUv() {
        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.eventTime().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                UserLogRecord::userId,
                                Collectors.collectingAndThen(Collectors.toSet(), users -> (long) users.size())
                        )
                ));
    }

    public FunnelStats funnelConversion() {
        Map<String, Set<Long>> usersByEvent = logs.stream()
                .collect(Collectors.groupingBy(
                        UserLogRecord::eventType,
                        Collectors.mapping(UserLogRecord::userId, Collectors.toSet())
                ));
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
    }

    public Map<String, Long> topCategories(int limit) {
        return logs.stream()
                .map(UserLogRecord::productCategory)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
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
