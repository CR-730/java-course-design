package edu.gpnu.bigdata.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceFactory {
    private DataSourceFactory() {
    }

    public static HikariDataSource create(ConfigLoader config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.get("db.url"));
        hikari.setUsername(config.get("db.username"));
        hikari.setPassword(config.get("db.password"));
        hikari.setMaximumPoolSize(Integer.parseInt(config.getOrDefault("db.maximumPoolSize", "10")));
        hikari.setPoolName("course-design-pool");
        return new HikariDataSource(hikari);
    }
}

