package it.raniero.fulcrum.api.database;

import it.raniero.fulcrum.api.database.properties.ConnectionType;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.database.redis.IRedisConnection;
import it.raniero.fulcrum.api.database.relational.RelationalConnection;
import java.util.Map;
import java.util.Optional;

/**
 * Registry and lifecycle manager for Fulcrum database connections.
 */
public interface IFulcrumDatabase {

    /**
     * Registers a connection from its database properties.
     *
     * @param properties connection properties
     */
    void registerConnection(DatabaseProperties properties);

    /**
     * Unregisters a connection by type and name.
     *
     * @param connectionType connection type
     * @param name connection name
     * @return {@code true} when a connection was removed
     */
    boolean unregisterConnection(ConnectionType connectionType, String name);

    /**
     * Gets a relational database connection by name.
     *
     * @param name connection name
     * @return matching relational connection
     */
    Optional<RelationalConnection> getDatabaseConnection(String name);

    /**
     * Gets a Redis connection by name.
     *
     * @param name connection name
     * @return matching Redis connection
     */
    Optional<IRedisConnection> getRedisConnection(String name);

    /**
     * Gets all registered relational connections keyed by name.
     *
     * @return relational connections
     */
    Map<String, RelationalConnection> getAllRelationalConnections();

    /**
     * Gets all registered Redis connections keyed by name.
     *
     * @return Redis connections
     */
    Map<String, IRedisConnection> getAllRedisConnections();

    /**
     * Closes all registered database connections.
     */
    void closeConnections();
}
