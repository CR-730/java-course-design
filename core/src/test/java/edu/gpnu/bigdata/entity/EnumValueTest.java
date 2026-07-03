package edu.gpnu.bigdata.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumValueTest {
    @Test
    void exposesStableEventAndChannelNames() {
        assertEquals("view", EventType.VIEW.value());
        assertEquals("pay", EventType.PAY.value());
        assertEquals(List.of("view", "cart", "order", "pay"), EventType.names());

        assertEquals("app", Channel.APP.value());
        assertEquals("miniprogram", Channel.MINIPROGRAM.value());
        assertEquals(List.of("app", "web", "miniprogram"), Channel.names());
    }
}
