package edu.gpnu.bigdata.util;

import edu.gpnu.bigdata.entity.Channel;
import edu.gpnu.bigdata.entity.EventType;
import edu.gpnu.bigdata.entity.UserLogRecord;
import edu.gpnu.bigdata.entity.UserRecord;
import net.datafaker.Faker;

import java.time.LocalDateTime;
import java.util.Locale;

public class SyntheticDataFactory {
    private static final String[] GENDERS = {"male", "female", "unknown"};
    private static final String[] DEVICES = {"android", "ios", "windows", "mac"};
    private static final String[] CATEGORIES = {"digital", "clothing", "book", "food", "sports"};

    private final int userCount;
    private final Faker faker;

    public SyntheticDataFactory(int userCount) {
        if (userCount <= 0) {
            throw new IllegalArgumentException("userCount must be positive");
        }
        this.userCount = userCount;
        this.faker = new Faker(Locale.CHINA);
    }

    public UserRecord userAt(int index) {
        validatePositive(index, "index");
        long id = index;
        String username = "user_%05d".formatted(index);
        String gender = GENDERS[index % GENDERS.length];
        int age = 18 + (index % 43);
        String channel = Channel.names().get(index % Channel.names().size());
        return new UserRecord(id, username, gender, age, channel);
    }

    public UserLogRecord logAt(int index) {
        validatePositive(index, "index");
        long userId = ((long) index * 37 % userCount) + 1;
        String eventType = eventTypeFor(index).value();
        String channel = Channel.names().get(index % Channel.names().size());
        String device = DEVICES[index % DEVICES.length];
        String category = categoryFor(index);
        LocalDateTime eventTime = LocalDateTime.now()
                .minusDays(index % 30L)
                .withHour(index % 24)
                .withMinute(index % 60)
                .withSecond(index % 60)
                .withNano(0);
        return new UserLogRecord(userId, eventType, eventTime, channel, device, category);
    }

    private EventType eventTypeFor(int index) {
        int bucket = index % 100;
        if (bucket < 55) {
            return EventType.VIEW;
        }
        if (bucket < 80) {
            return EventType.CART;
        }
        if (bucket < 93) {
            return EventType.ORDER;
        }
        return EventType.PAY;
    }

    private String categoryFor(int index) {
        if (index % 17 == 0) {
            return faker.commerce().department();
        }
        return CATEGORIES[index % CATEGORIES.length];
    }

    private static void validatePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}

