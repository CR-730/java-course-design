package edu.gpnu.bigdata.entity;

import java.time.LocalDateTime;

public record UserLogRecord(
        long userId,
        String eventType,
        LocalDateTime eventTime,
        String channel,
        String device,
        String productCategory
) {
}

