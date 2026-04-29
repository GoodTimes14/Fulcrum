package it.raniero.fulcrum.api;

import it.raniero.fulcrum.api.database.IFulcrumDatabase;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.utils.RedisProperties;

/**
 * Main entry point for starting, configuring, and accessing Fulcrum services.
 */
public interface FulcrumAPI {

    /**
     * Starts Fulcrum for the supplied host plugin.
     *
     * @param plugin plugin that owns this Fulcrum instance
     */
    void start(FulcrumPlugin plugin);

    /**
     * Gets the database service managed by Fulcrum.
     *
     * @return database service
     */
    IFulcrumDatabase getDatabase();

    /**
     * Gets the host plugin bound to this Fulcrum instance.
     *
     * @return host plugin
     */
    FulcrumPlugin getPlugin();

    /**
     * Creates and registers a relational database connection.
     *
     * @param databaseProperties relational database connection properties
     */
    void createDatabase(DatabaseProperties databaseProperties);

    /**
     * Creates and registers a Redis connection.
     *
     * @param redisProperties Redis connection properties
     */
    void createRedis(RedisProperties redisProperties);

    /**
     * Stops Fulcrum and releases managed resources.
     */
    void stop();
}
