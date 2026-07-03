package edu.gpnu.bigdata.service;

import edu.gpnu.bigdata.dto.StatsSnapshot;
import edu.gpnu.bigdata.entity.UserLogRecord;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CacheServiceTest {
    @Test
    void returnsEmptyWhenAnyStatsKeyIsMissing() {
        Jedis jedis = mock(Jedis.class);
        when(jedis.exists(
                CacheService.EVENT_TYPE_KEY,
                CacheService.CHANNEL_KEY,
                CacheService.DAILY_PV_KEY,
                CacheService.DAILY_UV_KEY,
                CacheService.FUNNEL_KEY,
                CacheService.TOP_CATEGORY_KEY
        )).thenReturn(5L);
        CacheService service = new CacheService(jedis);

        Optional<StatsSnapshot> snapshot = service.readStats();

        assertTrue(snapshot.isEmpty());
    }

    @Test
    void readsStatsSnapshotFromRedisHashes() {
        Jedis jedis = mock(Jedis.class);
        when(jedis.exists(
                CacheService.EVENT_TYPE_KEY,
                CacheService.CHANNEL_KEY,
                CacheService.DAILY_PV_KEY,
                CacheService.DAILY_UV_KEY,
                CacheService.FUNNEL_KEY,
                CacheService.TOP_CATEGORY_KEY
        )).thenReturn(6L);
        when(jedis.hgetAll(CacheService.EVENT_TYPE_KEY)).thenReturn(Map.of("view", "2", "pay", "1"));
        when(jedis.hgetAll(CacheService.CHANNEL_KEY)).thenReturn(Map.of("app", "3"));
        when(jedis.hgetAll(CacheService.DAILY_PV_KEY)).thenReturn(Map.of("2026-07-03", "3"));
        when(jedis.hgetAll(CacheService.DAILY_UV_KEY)).thenReturn(Map.of("2026-07-03", "2"));
        when(jedis.hgetAll(CacheService.FUNNEL_KEY)).thenReturn(Map.of(
                "viewUsers", "2",
                "cartUsers", "1",
                "orderUsers", "1",
                "payUsers", "1",
                "viewToCartRate", "50.0",
                "cartToOrderRate", "100.0",
                "orderToPayRate", "100.0"
        ));
        when(jedis.hgetAll(CacheService.TOP_CATEGORY_KEY)).thenReturn(Map.of("book", "3"));
        CacheService service = new CacheService(jedis);

        StatsSnapshot snapshot = service.readStats().orElseThrow();

        assertTrue(snapshot.fromCache());
        assertEquals(2, snapshot.eventTypeStats().get("view"));
        assertEquals(50.0, snapshot.funnelStats().viewToCartRate());
    }

    @Test
    void writesStatsSnapshotWithTtl() {
        Jedis jedis = mock(Jedis.class);
        CacheService service = new CacheService(jedis);

        service.writeStats(StatsSnapshot.from(new StatsService(sampleLogs()), false));

        verify(jedis).del(CacheService.EVENT_TYPE_KEY);
        verify(jedis).hset(org.mockito.ArgumentMatchers.eq(CacheService.EVENT_TYPE_KEY), anyMap());
        verify(jedis).expire(CacheService.EVENT_TYPE_KEY, 1800);
        verify(jedis).del(CacheService.FUNNEL_KEY);
        verify(jedis).hset(org.mockito.ArgumentMatchers.eq(CacheService.FUNNEL_KEY), anyMap());
        verify(jedis).expire(CacheService.FUNNEL_KEY, 1800);
    }

    private static List<UserLogRecord> sampleLogs() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 3, 10, 0);
        return List.of(
                new UserLogRecord(1, "view", now, "app", "android", "book"),
                new UserLogRecord(2, "view", now, "app", "ios", "book"),
                new UserLogRecord(1, "cart", now, "app", "android", "book"),
                new UserLogRecord(1, "order", now, "app", "android", "book"),
                new UserLogRecord(1, "pay", now, "app", "android", "book")
        );
    }
}
