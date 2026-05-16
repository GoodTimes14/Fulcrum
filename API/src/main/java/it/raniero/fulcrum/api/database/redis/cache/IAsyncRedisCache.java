package it.raniero.fulcrum.api.database.redis.cache;

import io.lettuce.core.RedisFuture;
import java.util.List;
import java.util.Map;

/**
 * Provides asynchronous Redis cache operations.
 */
public interface IAsyncRedisCache {

    /**
     * Gets a string value by key.
     *
     * @param key Redis key
     * @return future string value
     */
    RedisFuture<String> get(String key);

    /**
     * Deletes one or more keys.
     *
     * @param keys Redis keys
     * @return future number of deleted keys
     */
    RedisFuture<Long> del(String... keys);

    /**
     * Inserts a map at the given key.
     *
     * @param key Redis key
     * @param map map values
     * @return future Redis status
     */
    RedisFuture<String> insertMap(String key, Map<String, String> map);

    /**
     * Updates one field in a Redis map.
     *
     * @param key Redis key
     * @param field map field
     * @param value new field value
     * @return future update result
     */
    RedisFuture<Boolean> updateMap(String key, String field, String value);

    /**
     * Gets all fields from a Redis map.
     *
     * @param key Redis key
     * @return future map values
     */
    RedisFuture<Map<String, String>> getMap(String key);

    /**
     * Finds keys matching a pattern.
     *
     * @param pattern Redis key pattern
     * @return future matching keys
     */
    RedisFuture<List<String>> keys(String pattern);

    /**
     * Checks how many of the provided keys exist.
     *
     * @param key Redis key
     * @return future number of existing keys
     */
    RedisFuture<Long> exists(String key);

    /**
     * Sets an expiration on a key.
     *
     * @param key Redis key
     * @param seconds expiration time in seconds
     * @return future expiration result
     */
    RedisFuture<Boolean> initExpire(String key, int seconds);

    /**
     * Removes expiration from a key.
     *
     * @param key Redis key
     * @return future persistence result
     */
    RedisFuture<Boolean> persist(String key);

    /**
     * Sets a string value.
     *
     * @param key Redis key
     * @param value string value
     * @return future Redis status
     */
    RedisFuture<String> set(String key, String value);
}
