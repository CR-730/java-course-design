package edu.gpnu.bigdata.web;

import com.zaxxer.hikari.HikariDataSource;
import edu.gpnu.bigdata.service.CacheService;
import edu.gpnu.bigdata.service.CacheWarmupService;
import edu.gpnu.bigdata.service.StatsApplicationService;
import edu.gpnu.bigdata.util.ConfigLoader;
import edu.gpnu.bigdata.util.DataSourceFactory;
import edu.gpnu.bigdata.util.JedisFactory;
import io.javalin.Javalin;
import redis.clients.jedis.Jedis;

public final class ApiServer {
    private ApiServer() {
    }

    public static void main(String[] args) {
        ConfigLoader config = new ConfigLoader();
        HikariDataSource dataSource = DataSourceFactory.create(config);
        Jedis jedis = JedisFactory.create(config);
        StatsApplicationService statsService;
        try {
            statsService = new StatsApplicationService(dataSource.getConnection(), new CacheService(jedis));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start stats service", e);
        }

        Javalin app = Javalin.create();
        app.get("/api/health", ctx -> ctx.result("OK"));
        app.get("/api/stats", ctx -> ctx.json(statsService.stats()));
        app.get("/api/stats/event-type", ctx -> ctx.json(statsService.stats().eventTypeStats()));
        app.get("/api/stats/channel", ctx -> ctx.json(statsService.stats().channelStats()));
        app.get("/api/stats/funnel", ctx -> ctx.json(statsService.stats().funnelStats()));
        app.events(event -> event.serverStopped(() -> {
            jedis.close();
            dataSource.close();
        }));

        new CacheWarmupService(statsService).warmupAsync();
        app.start(8080);
    }
}
