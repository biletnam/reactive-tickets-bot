package com.github.bsnisar.tickets.misc;

public enum TemplateId {
    STATIONS_FOUND_ARRIVE("stations_found_to"),
    STATIONS_FOUND_DEPARTURE("stations_found_from"),

    ;

    public final String fileName;

    TemplateId(String fileName) {
        this.fileName = fileName;
    }
}
