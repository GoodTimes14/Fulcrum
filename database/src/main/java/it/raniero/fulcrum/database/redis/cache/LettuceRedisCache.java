package it.raniero.fulcrum.database.redis.cache;

import it.raniero.fulcrum.database.redis.LettuceConnection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LettuceRedisCache implements IRedisCache {

    private final LettuceConnection lettuceConnection;

    @Override
    public String fetch(String key) {

        if (exists(key)) {
            return lettuceConnection.getInteractionConnection().sync().get(key);
        }

        return "";
    }

    @Override
    public void delete(String... keys) {
        lettuceConnection.getInteractionConnection().sync().del(keys);
    }

    @Override
    public void insertMap(String key, Map<String, String> map) {
        lettuceConnection.getInteractionConnection().sync().hmset(key, map);
    }

    @Override
    public void updateMap(String key, String field, String value) {
        lettuceConnection.getInteractionConnection().sync().hset(key, field, value);
    }

    @Override
    public Map<String, String> getMap(String key) {
        return lettuceConnection.getInteractionConnection().sync().hgetall(key);
    }

    @Override
    public List<String> keys(String pattern) {
        return lettuceConnection.getInteractionConnection().sync().keys(pattern);
    }

    @Override
    public boolean exists(String key) {
        return lettuceConnection.getInteractionConnection().sync().exists(key) == 1;
    }

    @Override
    public void initExpire(String key, int seconds) {
        lettuceConnection.getInteractionConnection().sync().expire(key, seconds);
    }

    @Override
    public void persist(String key) {
        lettuceConnection.getInteractionConnection().sync().persist(key);
    }

    @Override
    public void set(String key, String str) {
        lettuceConnection.getInteractionConnection().sync().set(key, str);
    }
}
