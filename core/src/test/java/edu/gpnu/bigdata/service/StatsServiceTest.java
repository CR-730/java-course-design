package edu.gpnu.bigdata.service;

import edu.gpnu.bigdata.entity.UserLogRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatsServiceTest {
    @Test
    void countsByEventType() {
        StatsService service = new StatsService(sampleLogs());

        assertEquals(3, service.countByEventType().get("view"));
        assertEquals(2, service.countByEventType().get("cart"));
        assertEquals(1, service.countByEventType().get("order"));
        assertEquals(1, service.countByEventType().get("pay"));
    }

    @Test
    void calculatesFunnelConversionByDistinctUsers() {
        StatsService service = new StatsService(sampleLogs());

        var funnel = service.funnelConversion();

        assertEquals(3, funnel.viewUsers());
        assertEquals(2, funnel.cartUsers());
        assertEquals(1, funnel.orderUsers());
        assertEquals(1, funnel.payUsers());
        assertEquals(66.67, funnel.viewToCartRate());
        assertEquals(50.0, funnel.cartToOrderRate());
        assertEquals(100.0, funnel.orderToPayRate());
    }

    @Test
    void parallelEventTypeCountMatchesSequentialResult() {
        StatsService service = new StatsService(sampleLogs());

        assertEquals(service.countByEventType(), service.countByEventTypeParallel());
    }

    private static List<UserLogRecord> sampleLogs() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 2, 10, 0);
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
