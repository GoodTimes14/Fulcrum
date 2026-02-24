package it.raniero.fulcrum.bungeecord.utils;

import it.raniero.fulcrum.api.utils.MessageUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

public class BungeeUtils {

    private BungeeUtils() {
        throw new IllegalArgumentException();
    }

    public static TextComponent translateComponent(TextComponent inputComponent) {
        inputComponent.setText(MessageUtils.translateColors(inputComponent.getText()));
        if (inputComponent.getHoverEvent() != null
                && inputComponent.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {

            for (Content content : inputComponent.getHoverEvent().getContents()) {

                if (content instanceof Text text && text.getValue() instanceof BaseComponent[] components) {
                    for (BaseComponent component : components) {
                        if (component instanceof TextComponent textComponent) {
                            translateComponent(textComponent);
                        }
                    }
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
