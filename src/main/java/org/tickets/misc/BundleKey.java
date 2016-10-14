package org.tickets.misc;

public enum BundleKey {
    ROUTES_HELP("define.route.help"),
    STATION_NAME("station.name"),
    STATION_ID("station.id"),
    STATION_SEARCH_ERR("stations.search.error"),
    STATIONS_LIST("list.of.stations");

    public transient final String name;

    BundleKey(String name) {
        this.name = name;
    }
}
