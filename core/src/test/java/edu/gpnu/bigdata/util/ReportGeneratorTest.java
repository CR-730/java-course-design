package edu.gpnu.bigdata.util;

import edu.gpnu.bigdata.entity.UserLogRecord;
import edu.gpnu.bigdata.service.StatsService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportGeneratorTest {
    @Test
    void buildsReportWithCoreStatistics() {
        StatsService stats = new StatsService(List.of(
                log(1, "view", "app", "book"),
                log(1, "cart", "app", "book"),
                log(1, "order", "app", "book"),
                log(1, "pay", "app", "book"),
                log(2, "view", "web", "food")
        ));

        String report = ReportGenerator.buildReport(stats);

        assertTrue(report.contains("view: 2"));
        assertTrue(report.contains("pay: 1"));
        assertTrue(report.contains("app: 4"));
        assertTrue(report.contains("web: 1"));
        assertTrue(report.contains("view -> cart: 50.0%"));
        assertTrue(report.contains("book: 4"));
    }

    private static UserLogRecord log(long userId, String eventType, String channel, String category) {
        return new UserLogRecord(
                userId,
                eventType,
                LocalDateTime.of(2026, 7, 3, 10, 0),
                channel,
                "android",
                category
        );
    }
}
