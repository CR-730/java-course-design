package edu.gpnu.bigdata.service;

import edu.gpnu.bigdata.dao.UserLogDao;
import edu.gpnu.bigdata.dto.StatsSnapshot;

import java.sql.Connection;
import java.sql.SQLException;

public class StatsApplicationService {
    private final Connection connection;
    private final CacheService cacheService;
    private final UserLogDao userLogDao;

    public StatsApplicationService(Connection connection, CacheService cacheService) {
        this.connection = connection;
        this.cacheService = cacheService;
        this.userLogDao = new UserLogDao();
    }

    public StatsSnapshot stats() throws SQLException {
        return cacheService.readStats()
                .orElseGet(this::computeAndCache);
    }

    public StatsSnapshot refreshStats() {
        return computeAndCache();
    }

    private StatsSnapshot computeAndCache() {
        try {
            StatsSnapshot snapshot = StatsSnapshot.from(new StatsService(userLogDao.findAll(connection)), false);
            cacheService.writeStats(snapshot);
            return snapshot;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to compute stats", e);
        }
    }
}

