package it.raniero.fulcrum.api.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class MessageUtils {

    private MessageUtils() {
        throw new IllegalArgumentException("Utility Class");
    }

    /**
     * Yoinked from the bukkit API
     * @param textToTranslate
     * @return the text with the color code translated
     */
    public static String translateColors(String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; ++i) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static Component convertLegacyText(String input) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
    }
}
