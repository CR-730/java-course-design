package edu.gpnu.bigdata.dto;

import edu.gpnu.bigdata.entity.UserLogRecord;
import edu.gpnu.bigdata.service.StatsService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StatsSnapshotTest {
    @Test
    void createsSnapshotFromStatsService() {
        StatsService service = new StatsService(List.of(
                new UserLogRecord(1, "view", LocalDateTime.now(), "app", "ios", "book"),
                new UserLogRecord(1, "cart", LocalDateTime.now(), "app", "ios", "book")
        ));

        StatsSnapshot snapshot = StatsSnapshot.from(service, false);

        assertEquals(1, snapshot.eventTypeStats().get("view"));
        assertEquals(2, snapshot.channelStats().get("app"));
        assertEquals(2, snapshot.topCategoryStats().get("book"));
        assertFalse(snapshot.fromCache());
    }
}
