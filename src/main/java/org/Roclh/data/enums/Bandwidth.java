package org.Roclh.data.enums;

import lombok.Getter;

@Getter
public enum Bandwidth {
    MB4("4mbit"), MB8("8mbit"), MB16("16mbit"), MB32("32mbit"), MB64("64mbit");

    public String getBurst() {
        return (Long.parseLong(this.bandwidth.replaceAll("[a-zA-Z]", "")) / 4L) + "m";
    }

    private final String bandwidth;

    Bandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
    }
}