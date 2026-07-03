package edu.gpnu.bigdata.service;

import edu.gpnu.bigdata.dao.UserLogDao;
import edu.gpnu.bigdata.dto.StatsSnapshot;
import edu.gpnu.bigdata.entity.UserLogRecord;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatsApplicationServiceTest {
    @Test
    void returnsCachedStatsWithoutQueryingDao() throws SQLException {
        CacheService cacheService = mock(CacheService.class);
        UserLogDao userLogDao = mock(UserLogDao.class);
        StatsSnapshot cached = StatsSnapshot.from(new StatsService(sampleLogs()), true);
        when(cacheService.readStats()).thenReturn(Optional.of(cached));
        StatsApplicationService service = new StatsApplicationService(null, cacheService, userLogDao);

        StatsSnapshot actual = service.stats();

        assertSame(cached, actual);
        verify(userLogDao, never()).findAll(null);
    }

    @Test
    void computesAndCachesStatsWhenCacheMisses() throws SQLException {
        CacheService cacheService = mock(CacheService.class);
        UserLogDao userLogDao = mock(UserLogDao.class);
        when(cacheService.readStats()).thenReturn(Optional.empty());
        when(userLogDao.findAll(null)).thenReturn(sampleLogs());
        StatsApplicationService service = new StatsApplicationService(null, cacheService, userLogDao);

        StatsSnapshot snapshot = service.stats();

        assertFalse(snapshot.fromCache());
        assertEquals(2, snapshot.eventTypeStats().get("view"));
        verify(cacheService).writeStats(snapshot);
    }

    @Test
    void wrapsDaoFailureWhenComputingStats() throws SQLException {
        CacheService cacheService = mock(CacheService.class);
        UserLogDao userLogDao = mock(UserLogDao.class);
        when(cacheService.readStats()).thenReturn(Optional.empty());
        when(userLogDao.findAll(null)).thenThrow(new SQLException("database down"));
        StatsApplicationService service = new StatsApplicationService(null, cacheService, userLogDao);

        IllegalStateException error = assertThrows(IllegalStateException.class, service::stats);

        assertEquals("Failed to compute stats", error.getMessage());
        verify(cacheService, never()).writeStats(org.mockito.ArgumentMatchers.any());
    }

    private static List<UserLogRecord> sampleLogs() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 3, 10, 0);
        return List.of(
                new UserLogRecord(1, "view", now, "app", "android", "book"),
                new UserLogRecord(2, "view", now, "web", "windows", "food"),
                new UserLogRecord(1, "cart", now, "app", "android", "book"),
                new UserLogRecord(1, "order", now, "app", "android", "book"),
                new UserLogRecord(1, "pay", now, "app", "android", "book")
        );
    }
}
