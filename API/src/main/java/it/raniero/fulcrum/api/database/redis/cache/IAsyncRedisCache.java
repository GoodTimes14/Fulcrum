package it.raniero.fulcrum.api.database.redis.cache;

import io.lettuce.core.RedisFuture;
import java.util.List;
import java.util.Map;

public interface IAsyncRedisCache {

    RedisFuture<String> get(String key);

    RedisFuture<Long> del(String... keys);

    RedisFuture<String> insertMap(String key, Map<String, String> map);

    RedisFuture<Boolean> updateMap(String key, String field, String value);

    RedisFuture<Map<String, String>> getMap(String key);

    RedisFuture<List<String>> keys(String pattern);

    RedisFuture<Long> exists(String key);

    RedisFuture<Boolean> initExpire(String key, int seconds);

    RedisFuture<Boolean> persist(String key);

    RedisFuture<String> set(String key, String value);
}
