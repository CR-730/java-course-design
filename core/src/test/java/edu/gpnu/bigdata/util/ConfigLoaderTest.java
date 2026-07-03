package edu.gpnu.bigdata.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigLoaderTest {
    @Test
    void loadsConfiguredValuesAndDefaults() {
        ConfigLoader loader = new ConfigLoader();

        assertEquals("java-course-design", loader.get("app.name"));
        assertEquals("fallback", loader.getOrDefault("missing.key", "fallback"));
        assertEquals(6379, loader.getInt("redis.port"));
    }

    @Test
    void rejectsMissingResourceAndMissingKey() {
        IllegalArgumentException missingResource = assertThrows(
                IllegalArgumentException.class,
                () -> new ConfigLoader("missing.properties")
        );
        assertEquals("Missing resource: missing.properties", missingResource.getMessage());

        ConfigLoader loader = new ConfigLoader();
        IllegalArgumentException missingKey = assertThrows(
                IllegalArgumentException.class,
                () -> loader.get("missing.key")
        );
        assertEquals("Missing config key: missing.key", missingKey.getMessage());
    }
}
