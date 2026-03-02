package it.raniero.fulcrum.api.command.context.source;

import net.kyori.adventure.text.Component;

public interface FulcrumSource {

    Object getSourceObject();

    void sendMessage(String text);

    void sendMessage(Component component);

    SourceType sourceType();

    boolean hasPermission(String permissionNode);
}
