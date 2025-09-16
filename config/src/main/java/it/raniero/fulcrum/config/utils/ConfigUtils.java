package it.raniero.fulcrum.config.utils;

public class ConfigUtils {

    private ConfigUtils() {
        throw new IllegalArgumentException("Utility Class");
    }

    public static String createPath(String... strings) {
        return String.join("-", strings);
    }
}
