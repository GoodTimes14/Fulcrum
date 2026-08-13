package it.raniero.fulcrum.database.redis;

import io.lettuce.core.*;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import it.raniero.fulcrum.api.database.loqui.ILoqui;
import it.raniero.fulcrum.api.database.loqui.exception.LoquiDecoderException;
import it.raniero.fulcrum.api.database.loqui.message.LoquiEnvelope;
import it.raniero.fulcrum.api.database.loqui.message.LoquiMessage;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.database.redis.IRedisConnection;
import it.raniero.fulcrum.api.database.redis.Listen;
import it.raniero.fulcrum.api.database.redis.RedisListener;
import it.raniero.fulcrum.api.database.redis.cache.IAsyncRedisCache;
import it.raniero.fulcrum.api.database.redis.utils.RedisMethod;
import it.raniero.fulcrum.database.redis.cache.LettuceAsyncRedisCache;
import it.raniero.fulcrum.database.redis.cache.LettuceRedisCache;
import it.raniero.fulcrum.database.redis.listener.LettuceMessageListener;
import it.raniero.fulcrum.database.redis.loqui.FulcrumLoqui;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;

@Getter
public class LettuceConnection implements IRedisConnection {

    private Logger logger;
    private final LettuceRedisCache cache;
    private final LettuceAsyncRedisCache asyncCache;
    private final FulcrumLoqui loqui;

    private final Map<String, List<RedisMethod>> methodMap;
    private final DatabaseProperties properties;
    private RedisClient client;

    private StatefulRedisPubSubConnection<String, String> subscribeConnection;
    private StatefulRedisPubSubConnection<String, String> interactionConnection;

    public LettuceConnection(Logger logger, DatabaseProperties properties) {
        this.logger = logger;
        this.properties = properties;
        // Listeners are registered from the caller thread but read from the Redis I/O thread.
        methodMap = new ConcurrentHashMap<>();

        cache = new LettuceRedisCache(this);
        asyncCache = new LettuceAsyncRedisCache(this);
        loqui = new FulcrumLoqui(this);

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
            loqui.getChannels().remove(channel);
        }
    }

    @Override
    public void hopperMessage(String channel, String message) {
        List<RedisMethod> redisMethods = methodMap.get(channel);
        if (redisMethods == null || redisMethods.isEmpty()) {
            logger.log(Level.FINE, "Listeners not found");
            return;
        }

        boolean loquiChannel = loqui.getChannels().contains(channel);

        // Values a listener method can be fed with, in the order they are offered to it.
        Object[] arguments;
        if (loquiChannel) {

            try {
                LoquiEnvelope envelope = LoquiEnvelope.deserialize(message);
                // Remove echoing
                if (loqui.getSenderId().toString().equals(envelope.from())) {
                    return;
                }

                if (!loqui.isTargeted(channel, envelope.target())) {
                    return;
                }

                LoquiMessage decoded = loqui.decodeMessage(envelope);
                if (decoded == null) {
                    logger.log(Level.FINE, "No decoder registered for loqui packet id: " + envelope.packetId());
                }

                // A method asking for the envelope is served even when the payload can't be decoded.
                arguments = new Object[] {decoded, envelope};

            } catch (LoquiDecoderException e) {
                logger.warning("Received malformed loqui message from a loqui-registered channel, ignoring...");
                return;
            }
        } else {
            arguments = new Object[] {message};
        }

        for (RedisMethod redisMethod : redisMethods) {
            // A channel can carry several message types, only feed the ones the method actually accepts.
            Object argument = argumentFor(redisMethod.getMethod().getParameterTypes()[0], arguments);
            if (argument == null) {
                continue;
            }

            try {
                redisMethod.getMethod().invoke(redisMethod.getHolder(), argument);
            } catch (ReflectiveOperationException | RuntimeException e) {
                logger.log(
                        Level.SEVERE,
                        "Can't invoke method: " + redisMethod.getMethod().getName(),
                        e);
            }
        }
    }

    private Object argumentFor(Class<?> parameterType, Object[] arguments) {
        for (Object argument : arguments) {
            if (parameterType.isInstance(argument)) {
                return argument;
            }
        }

        return null;
    }

    @Override
    public void registerListener(RedisListener listener) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(Listen.class)) {
                Listen annotation = method.getAnnotation(Listen.class);

                if (method.getParameterCount() != 1) {
                    logger.warning("Skipping " + listener.getClass().getName() + "#" + method.getName()
                            + ": @Listen methods must accept exactly one parameter");
                    continue;
                }

                try {
                    // Public methods of non-public listener classes are not reflectively callable otherwise.
                    method.setAccessible(true);
                } catch (RuntimeException ignored) {
                    // Reported later if the invocation really fails.
                }

                methodMap
                        .computeIfAbsent(annotation.channel(), key -> new CopyOnWriteArrayList<>())
                        .add(new RedisMethod(listener, annotation, method));
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
    public ILoqui loqui() {
        return loqui;
    }

    @Override
    public void close() {
        loqui.close();
        subscribeConnection.close();
        interactionConnection.close();
        client.shutdown();
    }
}
