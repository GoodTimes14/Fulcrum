package it.raniero.fulcrum.api.server;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Adapts platform-specific server operations to the Fulcrum API.
 */
public interface FulcrumServer {

    /**
     * Gets the native command sender class for the platform.
     *
     * @return platform sender class
     */
    Class<?> getSenderClass();

    /**
     * Gets the native player class for the platform.
     *
     * @return platform player class
     */
    Class<?> getPlayerClass();

    /**
     * Gets online player names matching the provided pattern.
     *
     * @param pattern filter pattern
     * @return matching online player names
     */
    List<String> getOnlinePlayerNames(String pattern);

    /**
     * Finds an online player by name.
     *
     * @param name player name
     * @return matching player source, or {@code null} when unavailable
     */
    FulcrumSource getOnlinePlayer(String name);

    /**
     * Finds an online player by unique identifier.
     *
     * @param uuid player unique identifier
     * @return matching player source, or {@code null} when unavailable
     */
    FulcrumSource getOnlinePlayer(UUID uuid);

    /**
     * Gets the predicate used to determine whether a player is visible.
     *
     * @return player visibility predicate
     */
    Predicate<UUID> getPlayerVisibilityPredicate();

    /**
     * Sets the predicate used to determine whether a player is visible.
     *
     * @param predicate player visibility predicate
     */
    void setPlayerVisibilityPredicate(Predicate<UUID> predicate);
}
