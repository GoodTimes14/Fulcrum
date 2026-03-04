package it.raniero.fulcrum.api.database.redis;

import io.lettuce.core.RedisFuture;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.database.redis.cache.IAsyncRedisCache;
import it.raniero.fulcrum.api.database.redis.cache.IRedisCache;

public interface IRedisConnection {

    void connect(DatabaseProperties data);

    long publish(String channel, String message);

    RedisFuture<Long> publishAsync(String channel, String message);

    void subscribe(String channel);

    void hopperMessage(String channel, String message);

    void registerListener(RedisListener listener);

    default void registerListeners(RedisListener... listeners) {
        for (RedisListener listener : listeners) {
            registerListener(listener);
        }
    }

    RedisFuture<Void> subscribeAsync(String channel);

    IRedisCache cache();

    IAsyncRedisCache asyncCache();

    void close();
}
