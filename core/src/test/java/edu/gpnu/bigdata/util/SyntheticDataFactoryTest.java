package edu.gpnu.bigdata.util;

import edu.gpnu.bigdata.entity.Channel;
import edu.gpnu.bigdata.entity.EventType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyntheticDataFactoryTest {
    @Test
    void userAtUsesStableSequentialId() {
        SyntheticDataFactory factory = new SyntheticDataFactory(50_000);

        var user = factory.userAt(42);

        assertEquals(42L, user.id());
        assertEquals("user_00042", user.username());
        assertTrue(user.age() >= 18 && user.age() <= 60);
        assertTrue(Channel.names().contains(user.registerChannel()));
    }

    @Test
    void logAtKeepsValuesInsideTopicDEnums() {
        SyntheticDataFactory factory = new SyntheticDataFactory(50_000);

        var log = factory.logAt(100_001);

        assertTrue(log.userId() >= 1 && log.userId() <= 50_000);
        assertTrue(EventType.names().contains(log.eventType()));
        assertTrue(Channel.names().contains(log.channel()));
        assertFalse(log.device().isBlank());
        assertFalse(log.productCategory().isBlank());
    }
}

