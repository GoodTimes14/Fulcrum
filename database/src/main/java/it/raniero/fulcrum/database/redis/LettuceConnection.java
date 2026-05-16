package it.raniero.fulcrum.database.redis;

import io.lettuce.core.*;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.database.redis.IRedisConnection;
import it.raniero.fulcrum.api.database.redis.Listen;
import it.raniero.fulcrum.api.database.redis.RedisListener;
import it.raniero.fulcrum.api.database.redis.cache.IAsyncRedisCache;
import it.raniero.fulcrum.api.database.redis.utils.RedisMethod;
import it.raniero.fulcrum.database.redis.cache.LettuceAsyncRedisCache;
import it.raniero.fulcrum.database.redis.cache.LettuceRedisCache;
import it.raniero.fulcrum.database.redis.listener.LettuceMessageListener;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;

@Getter
public class LettuceConnection implements IRedisConnection {

    private Logger logger;
    private final LettuceRedisCache cache;
    private final LettuceAsyncRedisCache asyncCache;

    private final Map<String, List<RedisMethod>> methodMap;
    private final DatabaseProperties properties;
    private RedisClient client;

    private StatefulRedisPubSubConnection<String, String> subscribeConnection;
    private StatefulRedisPubSubConnection<String, String> interactionConnection;

    public LettuceConnection(Logger logger, DatabaseProperties properties) {
        this.logger = logger;
        this.properties = properties;
        methodMap = new HashMap<>();

        cache = new LettuceRedisCache(this);
        asyncCache = new LettuceAsyncRedisCache(this);
        connect(properties);
    }

    @Override
    public void connect(DatabaseProperties properties) {

        RedisURI uri = RedisURI.builder()
                .withHost(properties.getHost())
                .withPort(properties.getPort())
                .withSsl(properties.isSsl())
                .withStartTls(properties.isStartTls())
                .withVerifyPeer(properties.isVerifyPeer() ? SslVerifyMode.FULL : SslVerifyMode.NONE)
                .build();

        if (properties.isAuth()) {

            RedisCredentials credentials = RedisCredentials.just("default", properties.getPassword());
            uri.setCredentialsProvider(RedisCredentialsProvider.from(() -> credentials));
        }

        ClientResources res = DefaultClientResources.builder()
                .ioThreadPoolSize(2)
                .computationThreadPoolSize(4)
                .build();

        client = RedisClient.create(res, uri);

        client.setOptions(ClientOptions.builder()
                .autoReconnect(true)
                .protocolVersion(ProtocolVersion.RESP3)
                .build());

        subscribeConnection = client.connectPubSub();
        interactionConnection = client.connectPubSub();

        if (subscribeConnection != null) {
            LettuceMessageListener lettuceMessageListener = new LettuceMessageListener(this, subscribeConnection);
            subscribeConnection.addListener(lettuceMessageListener);
        }

        logger.log(Level.INFO, "[" + properties.getName() + "] " + "Connection to Redis established successfully!");
    }

    @Override
    public long publish(String channel, String message) {
        return interactionConnection.sync().publish(channel, message);
    }

    @Override
    public RedisFuture<Long> publishAsync(String channel, String message) {

        try {

            return interactionConnection.async().publish(channel, message);

        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Error while publishing message", exception);
        }

        return null;
    }

    @Override
    public void subscribe(String channel) {
        try {

            subscribeConnection.sync().subscribe(channel);

        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Error while subscribing", exception);
        }
    }

    @Override
    public void hopperMessage(String channel, String message) {
        if (!methodMap.containsKey(channel)) {
            logger.log(Level.FINE, "Listeners not found");
            return;
        }

        for (RedisMethod redisMethod : methodMap.get(channel)) {
            try {
                redisMethod.getMethod().invoke(redisMethod.getHolder(), message);
            } catch (ReflectiveOperationException e) {
                logger.log(
                        Level.SEVERE,
                        "Can't invoke method: " + redisMethod.getMethod().getName(),
                        e);
            }
        }
    }

    @Override
    public void registerListener(RedisListener listener) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(Listen.class)) {
                Listen annotation = method.getAnnotation(Listen.class);
                if (method.getParameterTypes().length != 1) {
                    continue;
                }
                if (methodMap.containsKey(annotation.channel())) {
                    methodMap.get(annotation.channel()).add(new RedisMethod(listener, annotation, method));
                } else {
                    methodMap.put(
                            annotation.channel(),
                            new ArrayList<>(Collections.singletonList(new RedisMethod(listener, annotation, method))));
                }
            }
        }
    }

    @Override
    public RedisFuture<Void> subscribeAsync(String channel) {
        try {

            return subscribeConnection.async().subscribe(channel);

        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Error while publishing message", exception);
        }
        return null;
    }

    @Override
    public LettuceRedisCache cache() {
        return cache;
    }

    @Override
    public IAsyncRedisCache asyncCache() {
        return asyncCache;
    }

    @Override
    public void close() {
        subscribeConnection.close();
        interactionConnection.close();
        client.shutdown();
    }
}
