package it.raniero.fulcrum.database.redis.cache;

import io.lettuce.core.RedisFuture;
import it.raniero.fulcrum.database.redis.LettuceConnection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LettuceAsyncRedisCache implements IAsyncRedisCache {

    private final LettuceConnection lettuceConnection;

    @Override
    public RedisFuture<String> get(String key) {
        return lettuceConnection.getInteractionConnection().async().get(key);
    }

    @Override
    public RedisFuture<Long> del(String... keys) {
        return lettuceConnection.getInteractionConnection().async().del(keys);
    }

    @Override
    public RedisFuture<String> insertMap(String key, Map<String, String> map) {
        return lettuceConnection.getInteractionConnection().async().hmset(key, map);
    }

    @Override
    public RedisFuture<Boolean> updateMap(String key, String field, String value) {
        return lettuceConnection.getInteractionConnection().async().hset(key, field, value);
    }

    @Override
    public RedisFuture<Map<String, String>> getMap(String key) {
        return lettuceConnection.getInteractionConnection().async().hgetall(key);
    }

    @Override
    public RedisFuture<List<String>> keys(String pattern) {
        return lettuceConnection.getInteractionConnection().async().keys(pattern);
    }

    @Override
    public RedisFuture<Long> exists(String key) {
        return lettuceConnection.getInteractionConnection().async().exists(key);
    }

    @Override
    public RedisFuture<Boolean> initExpire(String key, int seconds) {
        return lettuceConnection.getInteractionConnection().async().expire(key, seconds);
    }

    @Override
    public RedisFuture<Boolean> persist(String key) {
        return lettuceConnection.getInteractionConnection().async().persist(key);
    }

    @Override
    public RedisFuture<String> set(String key, String value) {
        return lettuceConnection.getInteractionConnection().async().set(key, value);
    }
}
