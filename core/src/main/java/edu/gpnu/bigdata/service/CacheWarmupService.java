package edu.gpnu.bigdata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CacheWarmupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheWarmupService.class);
    private final StatsApplicationService statsApplicationService;

    public CacheWarmupService(StatsApplicationService statsApplicationService) {
        this.statsApplicationService = statsApplicationService;
    }

    public CompletableFuture<Void> warmupAsync() {
        return CompletableFuture.runAsync(() -> {
                    LOGGER.info("Starting Redis cache warmup");
                    statsApplicationService.refreshStats();
                    LOGGER.info("Redis cache warmup completed");
                })
                .orTimeout(30, TimeUnit.SECONDS)
                .exceptionally(error -> {
                    LOGGER.warn("Redis cache warmup failed: {}", error.getMessage());
                    return null;
                });
    }
}

