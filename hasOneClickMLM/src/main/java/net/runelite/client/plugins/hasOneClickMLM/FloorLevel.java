package net.runelite.client.plugins.hasOneClickMLM;

import lombok.Getter;

@Getter
public enum FloorLevel {
    UPPER_LEVEL("Upper Level"),
    LOWER_LEVEL("Lower Level");

    private final String name;

    FloorLevel(String name) {
        this.name = name;

    }
}
