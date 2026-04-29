package it.raniero.fulcrum.api.command.context.source;

import java.util.UUID;
import net.kyori.adventure.text.Component;

/**
 * Wraps a platform-specific command source for Fulcrum commands.
 */
public interface FulcrumSource {

    /**
     * Gets the native platform source object.
     *
     * @return native source object
     */
    Object getSourceObject();

    /**
     * Sends a plain text message to this source.
     *
     * @param text message text
     */
    void sendMessage(String text);

    /**
     * Sends an Adventure component message to this source.
     *
     * @param component message component
     */
    void sendMessage(Component component);

    /**
     * Gets the source unique identifier.
     *
     * @return source unique identifier
     */
    UUID getUniqueId();

    /**
     * Gets the source display name.
     *
     * @return source name
     */
    String getName();

    /**
     * Gets the kind of source represented by this wrapper.
     *
     * @return source type
     */
    SourceType sourceType();

    /**
     * Checks whether this source has a permission.
     *
     * @param permissionNode permission node to check
     * @return {@code true} when permission is granted
     */
    boolean hasPermission(String permissionNode);
}
