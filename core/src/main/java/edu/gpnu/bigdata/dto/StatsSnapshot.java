package edu.gpnu.bigdata.dto;

import edu.gpnu.bigdata.service.StatsService;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public record StatsSnapshot(
        Map<String, Long> eventTypeStats,
        Map<String, Long> channelStats,
        Map<String, Long> dailyPv,
        Map<String, Long> dailyUv,
        FunnelStats funnelStats,
        Map<String, Long> topCategoryStats,
        boolean fromCache
) {
    public static StatsSnapshot from(StatsService statsService, boolean fromCache) {
        return new StatsSnapshot(
                statsService.countByEventType(),
                statsService.countByChannel(),
                statsService.dailyPv().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().toString(),
                                Map.Entry::getValue,
                                (left, right) -> left,
                                TreeMap::new
                        )),
                statsService.dailyUv().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().toString(),
                                Map.Entry::getValue,
                                (left, right) -> left,
                                TreeMap::new
                        )),
                statsService.funnelConversion(),
                statsService.topCategories(5),
                fromCache
        );
    }

    public StatsSnapshot withCacheFlag(boolean value) {
        return new StatsSnapshot(
                eventTypeStats,
                channelStats,
                dailyPv,
                dailyUv,
                funnelStats,
                topCategoryStats,
                value
        );
    }
}

