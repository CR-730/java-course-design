package edu.gpnu.bigdata.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private final Properties properties;

    public ConfigLoader() {
        this("application.properties");
    }

    public ConfigLoader(String resourceName) {
        this.properties = new Properties();
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing resource: " + resourceName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config: " + resourceName, e);
        }
    }

    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing config key: " + key);
        }
        return value.trim();
    }

    public String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue).trim();
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}

