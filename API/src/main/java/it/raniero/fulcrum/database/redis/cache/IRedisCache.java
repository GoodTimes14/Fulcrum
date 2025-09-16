package it.raniero.fulcrum.database.redis.cache;

import java.util.List;
import java.util.Map;

public interface IRedisCache {

    String fetch(String key);

    void delete(String... keys);

    void insertMap(String key, Map<String, String> map);

    void updateMap(String key, String field, String value);

    Map<String, String> getMap(String key);

    List<String> keys(String pattern);

    boolean exists(String key);

    void initExpire(String key, int seconds);

    void persist(String key);

    void set(String key, String str);
}
