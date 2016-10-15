package org.tickets.misc;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public enum BundleKey {
    ROUTES_HELP("define.route.help"),
    STATION_NAME("station.name"),
    STATION_ID("station.id"),
    STATION_SEARCH_ERR("stations.search.error"),
    STATIONS_FOUND_LIST("list.of.stations"),
    SECOND_ARGUMENT_REQUIRED("second.arg.need"),
    UNKNOWN_COMMAND("unknown.command");

    private static ResourceBundle DEFAULT_BUNDLE = ResourceBundle.getBundle("Messages");

    public transient final String name;

    BundleKey(String name) {
        this.name = name;
    }

    public String getText() {
        return DEFAULT_BUNDLE.getString(name);
    }

    public String getTemplateText(Object arg) {
        return MessageFormat.format(getText(), arg);
    }

}
