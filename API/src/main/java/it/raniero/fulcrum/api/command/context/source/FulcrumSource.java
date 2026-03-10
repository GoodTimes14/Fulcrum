package it.raniero.fulcrum.api.command.context.source;

import java.util.UUID;
import net.kyori.adventure.text.Component;

public interface FulcrumSource {

    Object getSourceObject();

    void sendMessage(String text);

    void sendMessage(Component component);

    UUID getUniqueId();

    String getName();

    SourceType sourceType();

    boolean hasPermission(String permissionNode);
}
