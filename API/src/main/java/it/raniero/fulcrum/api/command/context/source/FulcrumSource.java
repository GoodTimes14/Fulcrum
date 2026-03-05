package it.raniero.fulcrum.api.command.context.source;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface FulcrumSource {

    Object getSourceObject();

    void sendMessage(String text);

    void sendMessage(Component component);

    UUID getUniqueId();

    String getName();

    SourceType sourceType();

    boolean hasPermission(String permissionNode);
}
