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
- Accept exactly one argument. Ordinary channels supply a `String`; Loqui channels can supply a registered
  `LoquiMessage` subtype or `LoquiEnvelope`.

## Loqui Messaging and Discovery

Loqui adds typed sender/receiver messages and named endpoint discovery on top of the Redis connection.

### Receive a Decoded Message

Use a typed listener when the receiver only needs the decoded message content. A message is a small data holder that
rebuilds itself from the received content:

```java
public final class PlayerLookupMessage extends LoquiMessage {

    private UUID playerId;

    public PlayerLookupMessage(UUID playerId) {
        this.playerId = playerId;
    }

    public PlayerLookupMessage() {
        // Used by the registered receiver-side supplier.
    }

    public UUID playerId() {
        return playerId;
    }

    @Override
    public void deserialize(LoquiContent content) {
        playerId = UUID.fromString(content.getString("playerId"));
    }
}
```

A Redis listener can receive that concrete Loqui message type directly:

```java
public final class PlayerLookupListener implements RedisListener {

    private final Logger logger;

    public PlayerLookupListener(Logger logger) {
        this.logger = logger;
    }

    @Listen(channel = "network:requests")
    public void onPlayerLookup(PlayerLookupMessage message) {
        logger.info("Player lookup requested for " + message.playerId());
    }
}
```

Register the decoder and listener before marking and subscribing to the Pub/Sub channel as a Loqui channel:

```java
ILoqui loqui = redis.loqui();
loqui.registerMessageType(PlayerLookupMessage.class, PlayerLookupMessage::new);
redis.registerListener(new PlayerLookupListener(logger));
loqui.registerChannel("network:requests");
```

### Receive an Envelope

Use an envelope listener when the receiver needs routing metadata such as the sender, target, or packet id:

```java
public final class LoquiEnvelopeListener implements RedisListener {

    private final Logger logger;

    public LoquiEnvelopeListener(Logger logger) {
        this.logger = logger;
    }

    @Listen(channel = "network:requests")
    public void onEnvelope(LoquiEnvelope envelope) {
        logger.info("Received " + envelope.packetId()
                + " from " + envelope.from()
                + " for " + envelope.target());
    }
}
```

An envelope-only listener does not require a message decoder:

```java
ILoqui loqui = redis.loqui();
redis.registerListener(new LoquiEnvelopeListener(logger));
loqui.registerChannel("network:requests");
```

The envelope exposes the original JSON body through `serializedMessage()`. A valid envelope can therefore reach an
envelope listener even when its packet type has not been registered. If typed and envelope listeners are both
registered for the channel, both receive each matching message.

Both listener styles run only for envelopes addressed to this Loqui instance, to `BROADCAST`, or to a multicast
group the instance joined on that channel. Messages sent by the same instance are ignored to prevent echoes.

Messages can target every listener, a multicast group, or one discovered sender:

```java
loqui.broadcastMessage("network:requests", new PlayerLookupMessage(playerId));

loqui.registerMulticastGroup("network:requests", "lobbies");
loqui.sendMessage("network:requests", "lobbies", new PlayerLookupMessage(playerId));
```

Advertise a stable logical name when other processes need to address this instance directly:

```java
ILoquiDiscovery discovery = loqui.discovery();
discovery.setOptions(new LoquiDiscoveryOptions(
        15,    // heartbeat interval, seconds
        45,    // lease TTL, seconds
        false  // do not steal a live name
));

if (!discovery.advertise("lobby-eu-1")) {
    throw new IllegalStateException("Another Loqui instance owns lobby-eu-1");
}
```

Discovery stores one serialized value per server using ordinary Redis `GET`, `SET ... EX`, `KEYS`, and `DEL`
commands. Each `SET` atomically applies the configured lease TTL to the key, and every heartbeat refreshes both its
value and expiration. If an instance dies and stops sending heartbeats, Redis removes its advertisement
automatically. The timestamp in the value remains a second safeguard: stale records are ignored even before Redis
removes them. A consumer can resolve the logical name and use its sender id as the target:

```java
discovery.discover("lobby-eu-1").ifPresent(endpoint ->
        loqui.sendMessage(
                "network:requests",
                endpoint.senderId().toString(),
                new PlayerLookupMessage(playerId)));
```

`discoverAll()` returns an immutable snapshot of every live, supported discovery record. Missing, stale, malformed,
or newer-protocol records are ignored. Keep `aggressiveRetake` disabled unless forced takeover is intentional: when
enabled, an advertiser may replace an unexpired lease owned by another sender.

Call `stopAdvertising(name)` to release one name. Closing the Redis connection cancels the heartbeat and checks the
stored sender before removing each local advertisement.

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
