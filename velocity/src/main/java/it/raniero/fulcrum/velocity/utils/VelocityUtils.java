package it.raniero.fulcrum.velocity.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityUtils {

    private VelocityUtils() {
        throw new IllegalStateException("Utility Class");
    }

    public static Component convertLegacyText(String input) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
    }
}
