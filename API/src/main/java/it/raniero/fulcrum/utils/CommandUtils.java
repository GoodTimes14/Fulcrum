package it.raniero.fulcrum.utils;

import java.util.List;

public final class CommandUtils {

    private CommandUtils() {
        throw new IllegalArgumentException("Utility Class");
    }

    public static List<String> filterStringsByInput(String input, List<String> strings) {
        strings.removeIf(str -> !str.toLowerCase().startsWith(input.toLowerCase()));
        return strings;
    }
}
