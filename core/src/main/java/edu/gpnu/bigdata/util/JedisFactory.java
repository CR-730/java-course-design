package edu.gpnu.bigdata.util;

import redis.clients.jedis.Jedis;

public final class JedisFactory {
    private JedisFactory() {
    }

    public static Jedis create(ConfigLoader config) {
        Jedis jedis = new Jedis(
                config.getOrDefault("redis.host", "localhost"),
                Integer.parseInt(config.getOrDefault("redis.port", "6379")),
                Integer.parseInt(config.getOrDefault("redis.timeout", "2000"))
        );
        String password = config.getOrDefault("redis.password", "");
        if (!password.isBlank()) {
            jedis.auth(password);
        }
        return jedis;
    }
}

