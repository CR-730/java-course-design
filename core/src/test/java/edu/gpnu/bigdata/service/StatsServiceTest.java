package edu.gpnu.bigdata.service;

import edu.gpnu.bigdata.entity.UserLogRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    @Test
    void calculatesDailyPvUvAndTopCategories() {
        StatsService service = new StatsService(sampleLogs());

        LocalDate day = LocalDate.of(2026, 7, 2);
        assertEquals(7, service.dailyPv().get(day));
        assertEquals(3, service.dailyUv().get(day));
        assertEquals(Map.of("android", 4L, "windows", 2L, "ios", 1L), service.countByDevice());
        assertEquals(Map.of("view", 3L, "cart", 2L, "order", 1L, "pay", 1L), service.dailyEventTypeStats().get(day));
        assertEquals(Map.of("book", 6L, "food", 1L), service.topCategories(2));
    }

    @Test
    void calculatesFunnelDrilldownByChannelAndDevice() {
        StatsService service = new StatsService(sampleLogs());

        assertEquals(2, service.funnelByChannel().get("app").viewUsers());
        assertEquals(1, service.funnelByChannel().get("web").cartUsers());
        assertEquals(1, service.funnelByDevice().get("android").payUsers());
        assertEquals(0, service.funnelByDevice().get("ios").cartUsers());
    }

    @Test
    void handlesEmptyLogsWithoutDivisionByZero() {
        StatsService service = new StatsService(List.of());

        assertEquals(Map.of(), service.countByEventType());
        assertEquals(Map.of(), service.countByEventTypeParallel());
        assertEquals(0, service.funnelConversion().viewUsers());
        assertEquals(0.0, service.funnelConversion().viewToCartRate());
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
