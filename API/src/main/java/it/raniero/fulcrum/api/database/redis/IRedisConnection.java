package it.raniero.fulcrum.api.database.redis;

import io.lettuce.core.RedisFuture;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.database.redis.cache.IAsyncRedisCache;
import it.raniero.fulcrum.api.database.redis.cache.IRedisCache;

/**
 * Represents a Redis connection with publishing, subscribing, and cache access.
 */
public interface IRedisConnection {

    /**
     * Opens the Redis connection using database properties.
     *
     * @param data Redis connection properties
     */
    void connect(DatabaseProperties data);

    /**
     * Publishes a message to a Redis channel.
     *
     * @param channel Redis channel
     * @param message message payload
     * @return number of clients that received the message
     */
    long publish(String channel, String message);

    /**
     * Publishes a message to a Redis channel asynchronously.
     *
     * @param channel Redis channel
     * @param message message payload
     * @return future number of clients that received the message
     */
    RedisFuture<Long> publishAsync(String channel, String message);

    /**
     * Subscribes to a Redis channel.
     *
     * @param channel Redis channel
     */
    void subscribe(String channel);

    /**
     * Dispatches a received Redis message to registered listeners.
     *
     * @param channel Redis channel
     * @param message message payload
     */
    void hopperMessage(String channel, String message);

    /**
     * Registers a Redis listener.
     *
     * @param listener listener to register
     */
    void registerListener(RedisListener listener);

    /**
     * Registers multiple Redis listeners.
     *
     * @param listeners listeners to register
     */
    default void registerListeners(RedisListener... listeners) {
        for (RedisListener listener : listeners) {
            registerListener(listener);
        }
    }

    /**
     * Subscribes to a Redis channel asynchronously.
     *
     * @param channel Redis channel
     * @return completion future
     */
    RedisFuture<Void> subscribeAsync(String channel);

    /**
     * Gets the synchronous Redis cache API.
     *
     * @return synchronous Redis cache
     */
    IRedisCache cache();

    /**
     * Gets the asynchronous Redis cache API.
     *
     * @return asynchronous Redis cache
     */
    IAsyncRedisCache asyncCache();

    /**
     * Closes the Redis connection.
     */
    void close();
}
