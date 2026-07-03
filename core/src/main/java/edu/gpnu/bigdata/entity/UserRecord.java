package edu.gpnu.bigdata.entity;

public record UserRecord(
        long id,
        String username,
        String gender,
        int age,
        String registerChannel
) {
}

