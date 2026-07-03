package edu.gpnu.bigdata.service;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CacheWarmupServiceTest {
    @Test
    void refreshesStatsInBackground() {
        StatsApplicationService statsApplicationService = mock(StatsApplicationService.class);
        CacheWarmupService service = new CacheWarmupService(statsApplicationService);

        service.warmupAsync().join();

        verify(statsApplicationService).refreshStats();
    }
}
