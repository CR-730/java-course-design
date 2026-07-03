package edu.gpnu.bigdata.dto;

import edu.gpnu.bigdata.service.StatsService;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public record StatsSnapshot(
        Map<String, Long> eventTypeStats,
        Map<String, Long> channelStats,
        Map<String, Long> deviceStats,
        Map<String, Long> dailyPv,
        Map<String, Long> dailyUv,
        Map<String, Map<String, Long>> dailyEventTypeStats,
        FunnelStats funnelStats,
        Map<String, FunnelStats> channelFunnelStats,
        Map<String, FunnelStats> deviceFunnelStats,
        Map<String, Long> topCategoryStats,
        boolean fromCache
) {
    public static StatsSnapshot from(StatsService statsService, boolean fromCache) {
        return new StatsSnapshot(
                statsService.countByEventType(),
                statsService.countByChannel(),
                statsService.countByDevice(),
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
                statsService.dailyEventTypeStats().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().toString(),
                                Map.Entry::getValue,
                                (left, right) -> left,
                                TreeMap::new
                        )),
                statsService.funnelConversion(),
                statsService.funnelByChannel(),
                statsService.funnelByDevice(),
                statsService.topCategories(5),
                fromCache
        );
    }

    public StatsSnapshot withCacheFlag(boolean value) {
        return new StatsSnapshot(
                eventTypeStats,
                channelStats,
                deviceStats,
                dailyPv,
                dailyUv,
                dailyEventTypeStats,
                funnelStats,
                channelFunnelStats,
                deviceFunnelStats,
                topCategoryStats,
                value
        );
    }
}
