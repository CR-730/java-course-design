package edu.gpnu.bigdata.entity;

import java.util.List;

public enum Channel {
    APP("app"),
    WEB("web"),
    MINIPROGRAM("miniprogram");

    private final String value;

    Channel(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static List<String> names() {
        return List.of(APP.value, WEB.value, MINIPROGRAM.value);
    }
}

