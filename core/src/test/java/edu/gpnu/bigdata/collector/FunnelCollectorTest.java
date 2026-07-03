package edu.gpnu.bigdata.collector;

import edu.gpnu.bigdata.dto.FunnelStats;
import edu.gpnu.bigdata.entity.UserLogRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FunnelCollectorTest {
    @Test
    void collectsDistinctUsersForFunnelEvents() {
        FunnelStats stats = logs().stream().collect(new FunnelCollector());

        assertEquals(3, stats.viewUsers());
        assertEquals(2, stats.cartUsers());
        assertEquals(1, stats.orderUsers());
        assertEquals(1, stats.payUsers());
        assertEquals(66.67, stats.viewToCartRate());
    }

    @Test
    void worksWithParallelStream() {
        FunnelStats stats = logs().parallelStream().collect(new FunnelCollector());

        assertEquals(3, stats.viewUsers());
        assertEquals(2, stats.cartUsers());
        assertEquals(1, stats.orderUsers());
        assertEquals(1, stats.payUsers());
    }

    private static List<UserLogRecord> logs() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 3, 10, 0);
        return List.of(
                new UserLogRecord(1, "view", now, "app", "android", "book"),
                new UserLogRecord(2, "view", now, "web", "windows", "book"),
                new UserLogRecord(3, "view", now, "app", "ios", "food"),
                new UserLogRecord(1, "cart", now, "app", "android", "book"),
                new UserLogRecord(2, "cart", now, "web", "windows", "book"),
                new UserLogRecord(1, "order", now, "app", "android", "book"),
                new UserLogRecord(1, "pay", now, "app", "android", "book")
        );
    }
}

