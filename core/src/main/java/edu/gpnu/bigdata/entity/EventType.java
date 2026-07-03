package edu.gpnu.bigdata.entity;

import java.util.List;

public enum EventType {
    VIEW("view"),
    CART("cart"),
    ORDER("order"),
    PAY("pay");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static List<String> names() {
        return List.of(VIEW.value, CART.value, ORDER.value, PAY.value);
    }
}

