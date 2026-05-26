# Redis Handling

Fulcrum exposes Redis through `IRedisConnection`. You can use it as a cache, a simple key-value store, or a Pub/Sub bus between plugins and services.

## Configure a Redis Connection

In `databases.yml`:

```yaml
databases:
  - name: cache
    enabled: true
    connectionType: REDIS
    host: localhost
    port: 6379
    password: change-me
    ssl: false
    verifyPeer: true
    startTls: false
```

Use `ssl: true` and keep `verifyPeer: true` when connecting over an untrusted network.

## Get a Redis Connection

```java
IRedisConnection redis = fulcrum.getDatabase()
        .getRedisConnection("cache")
        .orElseThrow(() -> new IllegalStateException("Redis 'cache' is not configured"));
```

## Store and Fetch Strings

```java
public void cachePlayerName(Fulcrum fulcrum, UUID uuid, String name) {
    IRedisConnection redis = fulcrum.getDatabase()
            .getRedisConnection("cache")
            .orElseThrow();

    redis.cache().set("player:" + uuid + ":name", name);
    redis.cache().initExpire("player:" + uuid + ":name", 3600);
}
```

```java
public Optional<String> getCachedPlayerName(Fulcrum fulcrum, UUID uuid) {
    IRedisConnection redis = fulcrum.getDatabase()
            .getRedisConnection("cache")
            .orElseThrow();

    String value = redis.cache().fetch("player:" + uuid + ":name");
    return value.isEmpty() ? Optional.empty() : Optional.of(value);
}
```

`fetch` returns an empty string when the key does not exist.

## Store Hash Maps

```java
public void cacheProfile(Fulcrum fulcrum, UUID uuid, String name, String rank) {
    IRedisConnection redis = fulcrum.getDatabase()
            .getRedisConnection("cache")
            .orElseThrow();

    Map<String, String> profile = new HashMap<>();
    profile.put("name", name);
    profile.put("rank", rank);

    String key = "profile:" + uuid;
    redis.cache().insertMap(key, profile);
    redis.cache().initExpire(key, 600);
}
```

```java
public Map<String, String> getCachedProfile(Fulcrum fulcrum, UUID uuid) {
    IRedisConnection redis = fulcrum.getDatabase()
            .getRedisConnection("cache")
            .orElseThrow();

    return redis.cache().getMap("profile:" + uuid);
}
```

## Use Async Redis Operations

The async cache API returns Lettuce `RedisFuture` values:

```java
public void saveSessionAsync(Fulcrum fulcrum, UUID uuid, String serverName) {
    IRedisConnection redis = fulcrum.getDatabase()
            .getRedisConnection("cache")
            .orElseThrow();

    redis.asyncCache()
            .set("session:" + uuid, serverName)
            .thenAccept(status -> fulcrum.getPlugin()
                    .getLogger()
                    .fine("Redis SET status: " + status));
}
```

You can compose these futures, but keep platform threading rules in mind before touching server APIs from callbacks.

## Publish Messages

```java
public void announceRestart(Fulcrum fulcrum, String serverName) {
    IRedisConnection redis = fulcrum.getDatabase()
            .getRedisConnection("cache")
            .orElseThrow();

    redis.publish("network:restart", serverName);
}
```

Async publish:

```java
redis.publishAsync("network:restart", serverName)
        .thenAccept(receivers -> logger.info("Restart notice delivered to " + receivers + " listeners"));
```

## Subscribe With a Listener

Create a listener class:

```java
public final class NetworkListener implements RedisListener {

    private final Logger logger;

    public NetworkListener(Logger logger) {
        this.logger = logger;
    }

    @Listen(channel = "network:restart")
    public void onRestartMessage(String serverName) {
        logger.info("Restart requested by " + serverName);
    }
}
```

Register and subscribe during startup:

```java
public void registerRedisListeners(Fulcrum fulcrum) {
    IRedisConnection redis = fulcrum.getDatabase()
            .getRedisConnection("cache")
            .orElseThrow();

    redis.registerListener(new NetworkListener(fulcrum.getPlugin().getLogger()));
    redis.subscribe("network:restart");
}
```

Listener methods must:

- Be public.
- Be annotated with `@Listen(channel = "...")`.
- Accept exactly one `String` argument.

## Practical Message Format

For anything more complex than a single string, use a small structured payload. For example:

```java
String message = uuid + ";" + serverName;
redis.publish("player:switch", message);
```

Then validate the payload before using it:

```java
@Listen(channel = "player:switch")
public void onPlayerSwitch(String message) {
    String[] parts = message.split(";", 2);
    if (parts.length != 2) {
        return;
    }

    UUID uuid;
    try {
        uuid = UUID.fromString(parts[0]);
    } catch (IllegalArgumentException ex) {
        return;
    }

    String serverName = parts[1];
    // Handle validated values.
}
```

## Avoid Broad Key Searches

The current cache API exposes `keys(pattern)`:

```java
List<String> sessions = redis.cache().keys("session:*");
```

Use it only for administrative or small local datasets. Redis `KEYS` scans the whole keyspace and can block the Redis server on large datasets. Prefer designing keys so you can fetch them directly, or maintain an index set in Redis from your application logic.

## Cleanup

Redis connections close when Fulcrum stops:

```java
fulcrum.stop();
```

To close a single Redis connection manually:

```java
fulcrum.getDatabase().unregisterConnection(ConnectionType.REDIS, "cache");
```

