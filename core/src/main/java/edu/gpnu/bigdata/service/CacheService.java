package edu.gpnu.bigdata.service;

import edu.gpnu.bigdata.dto.FunnelStats;
import edu.gpnu.bigdata.dto.StatsSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CacheService {
    public static final String EVENT_TYPE_KEY = "stats:eventType";
    public static final String CHANNEL_KEY = "stats:channel";
    public static final String DEVICE_KEY = "stats:device";
    public static final String DAILY_PV_KEY = "stats:dailyPv";
    public static final String DAILY_UV_KEY = "stats:dailyUv";
    public static final String DAILY_EVENT_TYPE_KEY = "stats:dailyEventType";
    public static final String FUNNEL_KEY = "stats:funnel";
    public static final String CHANNEL_FUNNEL_KEY = "stats:channelFunnel";
    public static final String DEVICE_FUNNEL_KEY = "stats:deviceFunnel";
    public static final String TOP_CATEGORY_KEY = "stats:topCategory";
    private static final int TTL_SECONDS = 1800;
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

    private final Jedis jedis;

    public CacheService(Jedis jedis) {
        this.jedis = jedis;
    }

    public synchronized Optional<StatsSnapshot> readStats() {
        if (jedis.exists(
                EVENT_TYPE_KEY,
                CHANNEL_KEY,
                DEVICE_KEY,
                DAILY_PV_KEY,
                DAILY_UV_KEY,
                DAILY_EVENT_TYPE_KEY,
                FUNNEL_KEY,
                CHANNEL_FUNNEL_KEY,
                DEVICE_FUNNEL_KEY,
                TOP_CATEGORY_KEY
        ) != 10) {
            LOGGER.info("Redis cache miss for stats snapshot");
            return Optional.empty();
        }
        LOGGER.info("Redis cache hit for stats snapshot");
        Map<String, String> funnel = jedis.hgetAll(FUNNEL_KEY);
        return Optional.of(new StatsSnapshot(
                readLongMap(EVENT_TYPE_KEY),
                readLongMap(CHANNEL_KEY),
                readLongMap(DEVICE_KEY),
                readLongMap(DAILY_PV_KEY),
                readLongMap(DAILY_UV_KEY),
                readNestedLongMap(DAILY_EVENT_TYPE_KEY),
                new FunnelStats(
                        parseLong(funnel, "viewUsers"),
                        parseLong(funnel, "cartUsers"),
                        parseLong(funnel, "orderUsers"),
                        parseLong(funnel, "payUsers"),
                        parseDouble(funnel, "viewToCartRate"),
                        parseDouble(funnel, "cartToOrderRate"),
                        parseDouble(funnel, "orderToPayRate")
                ),
                readFunnelMap(CHANNEL_FUNNEL_KEY),
                readFunnelMap(DEVICE_FUNNEL_KEY),
                readLongMap(TOP_CATEGORY_KEY),
                true
        ));
    }

    public synchronized void writeStats(StatsSnapshot snapshot) {
        writeLongMap(EVENT_TYPE_KEY, snapshot.eventTypeStats());
        writeLongMap(CHANNEL_KEY, snapshot.channelStats());
        writeLongMap(DEVICE_KEY, snapshot.deviceStats());
        writeLongMap(DAILY_PV_KEY, snapshot.dailyPv());
        writeLongMap(DAILY_UV_KEY, snapshot.dailyUv());
        writeNestedLongMap(DAILY_EVENT_TYPE_KEY, snapshot.dailyEventTypeStats());
        writeFunnel(snapshot.funnelStats());
        writeFunnelMap(CHANNEL_FUNNEL_KEY, snapshot.channelFunnelStats());
        writeFunnelMap(DEVICE_FUNNEL_KEY, snapshot.deviceFunnelStats());
        writeLongMap(TOP_CATEGORY_KEY, snapshot.topCategoryStats());
        LOGGER.info("Redis cache refreshed for stats snapshot");
    }

    private Map<String, Long> readLongMap(String key) {
        return jedis.hgetAll(key).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Long.parseLong(entry.getValue()),
                        (left, right) -> left,
                        HashMap::new
                ));
    }

    private Map<String, Map<String, Long>> readNestedLongMap(String key) {
        return jedis.hgetAll(key).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> parseLongMapValue(entry.getValue()),
                        (left, right) -> left,
                        HashMap::new
                ));
    }

    private Map<String, FunnelStats> readFunnelMap(String key) {
        return jedis.hgetAll(key).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> parseFunnelValue(entry.getValue()),
                        (left, right) -> left,
                        HashMap::new
                ));
    }

    private void writeLongMap(String key, Map<String, Long> values) {
        jedis.del(key);
        if (!values.isEmpty()) {
            jedis.hset(key, values.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue()))));
        }
        jedis.expire(key, TTL_SECONDS);
    }

    private void writeNestedLongMap(String key, Map<String, Map<String, Long>> values) {
        jedis.del(key);
        if (!values.isEmpty()) {
            jedis.hset(key, values.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> formatLongMapValue(entry.getValue()))));
        }
        jedis.expire(key, TTL_SECONDS);
    }

    private void writeFunnel(FunnelStats stats) {
        jedis.del(FUNNEL_KEY);
        jedis.hset(FUNNEL_KEY, Map.of(
                "viewUsers", String.valueOf(stats.viewUsers()),
                "cartUsers", String.valueOf(stats.cartUsers()),
                "orderUsers", String.valueOf(stats.orderUsers()),
                "payUsers", String.valueOf(stats.payUsers()),
                "viewToCartRate", String.valueOf(stats.viewToCartRate()),
                "cartToOrderRate", String.valueOf(stats.cartToOrderRate()),
                "orderToPayRate", String.valueOf(stats.orderToPayRate())
        ));
        jedis.expire(FUNNEL_KEY, TTL_SECONDS);
    }

    private void writeFunnelMap(String key, Map<String, FunnelStats> values) {
        jedis.del(key);
        if (!values.isEmpty()) {
            jedis.hset(key, values.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> formatFunnelValue(entry.getValue()))));
        }
        jedis.expire(key, TTL_SECONDS);
    }

    private static long parseLong(Map<String, String> values, String key) {
        return Long.parseLong(values.getOrDefault(key, "0"));
    }

    private static double parseDouble(Map<String, String> values, String key) {
        return Double.parseDouble(values.getOrDefault(key, "0"));
    }

    private static String formatLongMapValue(Map<String, Long> values) {
        return values.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
    }

    private static Map<String, Long> parseLongMapValue(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(part -> part.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> pair[0],
                        pair -> Long.parseLong(pair[1]),
                        (left, right) -> left,
                        HashMap::new
                ));
    }

    private static String formatFunnelValue(FunnelStats stats) {
        return stats.viewUsers() + ","
                + stats.cartUsers() + ","
                + stats.orderUsers() + ","
                + stats.payUsers() + ","
                + stats.viewToCartRate() + ","
                + stats.cartToOrderRate() + ","
                + stats.orderToPayRate();
    }

    private static FunnelStats parseFunnelValue(String value) {
        String[] parts = value.split(",", -1);
        return new FunnelStats(
                Long.parseLong(parts[0]),
                Long.parseLong(parts[1]),
                Long.parseLong(parts[2]),
                Long.parseLong(parts[3]),
                Double.parseDouble(parts[4]),
                Double.parseDouble(parts[5]),
                Double.parseDouble(parts[6])
        );
    }
}
