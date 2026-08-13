package it.raniero.fulcrum.database.redis.loqui.discovery;

import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryData;
import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryStore;
import it.raniero.fulcrum.api.database.loqui.exception.LoquiDiscoveryException;
import it.raniero.fulcrum.api.database.redis.cache.IRedisCache;
import it.raniero.fulcrum.database.redis.LettuceConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Redis string-key implementation of the Loqui discovery store. */
public class RedisLoquiDiscoveryStore implements LoquiDiscoveryStore {

    static final String KEY_PREFIX = "fulcrum:loqui:discovery:";

    private final LettuceConnection connection;

    public RedisLoquiDiscoveryStore(LettuceConnection connection) {
        this.connection = connection;
    }

    @Override
    public void set(String name, LoquiDiscoveryData data) {
        try {
            cache().set(key(name), data.serialize(), data.updateTTL());
        } catch (RuntimeException e) {
            throw operationFailed("set", name, e);
        }
    }

    @Override
    public Optional<LoquiDiscoveryData> get(String name) {
        try {
            String serialized = cache().fetch(key(name));
            if (serialized == null || serialized.isBlank()) {
                return Optional.empty();
            }

            try {
                return Optional.of(LoquiDiscoveryData.deserialize(serialized));
            } catch (IllegalArgumentException ignored) {
                return Optional.empty();
            }
        } catch (RuntimeException e) {
            throw operationFailed("get", name, e);
        }
    }

    @Override
    public Map<String, LoquiDiscoveryData> getAll() {
        try {
            Map<String, LoquiDiscoveryData> discovered = new HashMap<>();
            for (String key : cache().keys(KEY_PREFIX + "*")) {
                if (key.length() <= KEY_PREFIX.length()) {
                    continue;
                }

                String name = key.substring(KEY_PREFIX.length());
                get(name).ifPresent(data -> discovered.put(name, data));
            }
            return discovered;
        } catch (RuntimeException e) {
            if (e instanceof LoquiDiscoveryException discoveryException) {
                throw discoveryException;
            }
            throw new LoquiDiscoveryException("Can't list Loqui discovery advertisements", e);
        }
    }

    @Override
    public void delete(String name) {
        try {
            cache().delete(key(name));
        } catch (RuntimeException e) {
            throw operationFailed("delete", name, e);
        }
    }

    private IRedisCache cache() {
        if (connection == null || connection.getInteractionConnection() == null) {
            throw new LoquiDiscoveryException("Loqui discovery requires an open Redis connection");
        }
        return connection.cache();
    }

    private String key(String name) {
        return KEY_PREFIX + name;
    }

    private LoquiDiscoveryException operationFailed(String operation, String name, RuntimeException cause) {
        if (cause instanceof LoquiDiscoveryException discoveryException) {
            return discoveryException;
        }
        return new LoquiDiscoveryException("Can't " + operation + " Loqui discovery advertisement: " + name, cause);
    }
}
