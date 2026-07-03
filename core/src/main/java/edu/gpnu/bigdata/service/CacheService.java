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
    public static final String DAILY_PV_KEY = "stats:dailyPv";
    public static final String DAILY_UV_KEY = "stats:dailyUv";
    public static final String FUNNEL_KEY = "stats:funnel";
    public static final String TOP_CATEGORY_KEY = "stats:topCategory";
    private static final int TTL_SECONDS = 1800;
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

    private final Jedis jedis;

    public CacheService(Jedis jedis) {
        this.jedis = jedis;
    }

    public Optional<StatsSnapshot> readStats() {
        if (jedis.exists(EVENT_TYPE_KEY, CHANNEL_KEY, DAILY_PV_KEY, DAILY_UV_KEY, FUNNEL_KEY, TOP_CATEGORY_KEY) != 6) {
            LOGGER.info("Redis cache miss for stats snapshot");
            return Optional.empty();
        }
        LOGGER.info("Redis cache hit for stats snapshot");
        Map<String, String> funnel = jedis.hgetAll(FUNNEL_KEY);
        return Optional.of(new StatsSnapshot(
                readLongMap(EVENT_TYPE_KEY),
                readLongMap(CHANNEL_KEY),
                readLongMap(DAILY_PV_KEY),
                readLongMap(DAILY_UV_KEY),
                new FunnelStats(
                        parseLong(funnel, "viewUsers"),
                        parseLong(funnel, "cartUsers"),
                        parseLong(funnel, "orderUsers"),
                        parseLong(funnel, "payUsers"),
                        parseDouble(funnel, "viewToCartRate"),
                        parseDouble(funnel, "cartToOrderRate"),
                        parseDouble(funnel, "orderToPayRate")
                ),
                readLongMap(TOP_CATEGORY_KEY),
                true
        ));
    }

    public void writeStats(StatsSnapshot snapshot) {
        writeLongMap(EVENT_TYPE_KEY, snapshot.eventTypeStats());
        writeLongMap(CHANNEL_KEY, snapshot.channelStats());
        writeLongMap(DAILY_PV_KEY, snapshot.dailyPv());
        writeLongMap(DAILY_UV_KEY, snapshot.dailyUv());
        writeFunnel(snapshot.funnelStats());
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

    private void writeLongMap(String key, Map<String, Long> values) {
        jedis.del(key);
        if (!values.isEmpty()) {
            jedis.hset(key, values.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue()))));
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

    private static long parseLong(Map<String, String> values, String key) {
        return Long.parseLong(values.getOrDefault(key, "0"));
    }

    private static double parseDouble(Map<String, String> values, String key) {
        return Double.parseDouble(values.getOrDefault(key, "0"));
    }
}
