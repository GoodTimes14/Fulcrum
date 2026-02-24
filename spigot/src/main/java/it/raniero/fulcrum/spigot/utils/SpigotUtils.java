package it.raniero.fulcrum.spigot.utils;

import it.raniero.fulcrum.api.utils.MessageUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SpigotUtils {

    private SpigotUtils() {
        throw new IllegalArgumentException();
    }

    public static TextComponent translateComponent(TextComponent inputComponent) {
        inputComponent.setText(MessageUtils.translateColors(inputComponent.getText()));
        if (inputComponent.getHoverEvent() != null
                && inputComponent.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {

            for (BaseComponent component : inputComponent.getHoverEvent().getValue()) {
                if (component instanceof TextComponent textComponent) {
                    translateComponent(textComponent);
                }
            }
        }

        if (inputComponent.getExtra() != null) {
            for (BaseComponent component : inputComponent.getExtra()) {
                if (component instanceof TextComponent textComponent) {
                    translateComponent(textComponent);
                }
            }
        }

        return inputComponent;
    }
}
