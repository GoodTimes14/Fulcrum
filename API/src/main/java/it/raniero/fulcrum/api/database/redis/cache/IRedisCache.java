package it.raniero.fulcrum.api.database.redis.cache;

import java.util.List;
import java.util.Map;

/**
 * Provides synchronous Redis cache operations.
 */
public interface IRedisCache {

    /**
     * Fetches a string value by key.
     *
     * @param key Redis key
     * @return string value
     */
    String fetch(String key);

    /**
     * Deletes one or more keys.
     *
     * @param keys Redis keys
     */
    void delete(String... keys);

    /**
     * Inserts a map at the given key.
     *
     * @param key Redis key
     * @param map map values
     */
    void insertMap(String key, Map<String, String> map);

    /**
     * Updates one field in a Redis map.
     *
     * @param key Redis key
     * @param field map field
     * @param value new field value
     */
    void updateMap(String key, String field, String value);

    /**
     * Gets all fields from a Redis map.
     *
     * @param key Redis key
     * @return map values
     */
    Map<String, String> getMap(String key);

    /**
     * Finds keys matching a pattern.
     *
     * @param pattern Redis key pattern
     * @return matching keys
     */
    List<String> keys(String pattern);

    /**
     * Checks whether a key exists.
     *
     * @param key Redis key
     * @return {@code true} when the key exists
     */
    boolean exists(String key);

    /**
     * Sets an expiration on a key.
     *
     * @param key Redis key
     * @param seconds expiration time in seconds
     */
    void initExpire(String key, int seconds);

    /**
     * Removes expiration from a key.
     *
     * @param key Redis key
     */
    void persist(String key);

    /**
     * Sets a string value.
     *
     * @param key Redis key
     * @param str string value
     */
    void set(String key, String str);

    /**
     * Sets a string value with an expiration.
     *
     * <p>The default implementation is provided for compatibility with cache implementations that only expose
     * separate set and expire operations. Implementations should override this method when the backing store can
     * apply the value and expiration atomically.
     *
     * @param key Redis key
     * @param str string value
     * @param expirationSeconds expiration time in seconds
     */
    default void set(String key, String str, long expirationSeconds) {
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("Redis expiration must be positive");
        }

        final int legacyExpiration;
        try {
            legacyExpiration = Math.toIntExact(expirationSeconds);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Redis expiration exceeds the supported range", e);
        }

        set(key, str);
        initExpire(key, legacyExpiration);
    }
}
