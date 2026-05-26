# Fulcrum Usage Guide

Fulcrum is a small platform layer for Minecraft plugins and standalone terminal tools. It gives you:

- A shared command model for Spigot, BungeeCord, Velocity, and the terminal runner.
- A database registry for SQL and Redis connections.
- Argument conversion and platform-independent command sources.

This guide is split by the parts of the library you normally use directly:

- [Gradle Installation](installation.md)
- [Relational Database Handling](database-sql.md)
- [Redis Handling](database-redis.md)
- [Command Handling](commands.md)

## Runtime Model

Each platform module creates a `Fulcrum` instance, starts it with the platform plugin, then registers commands through the command manager:

```java
private Fulcrum fulcrum;

public void onEnable() {
    fulcrum = new Fulcrum();
    fulcrum.start(this);

    fulcrum.getCommandManager().wrapCommand(new MyCommand(fulcrum));
}

public void onDisable() {
    fulcrum.stop();
}
```

Platform adapters implement `FulcrumPlugin`, so Fulcrum can access:

- The plugin data folder.
- The logger.
- The platform command register.
- The platform server adapter.

On startup, Fulcrum creates `messages.yml` and `databases.yml`, registers enabled database connections, and initializes command argument converters.

## Generated Database Config

Fulcrum reads database connections from `databases.yml`. A practical SQL and Redis setup looks like this:

```yaml
databases:
  - name: main
    enabled: true
    connectionType: SQL
    host: localhost
    port: 3306
    database: fulcrum
    username: fulcrum_app
    password: change-me

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

The `name` field is the key you use later:

```java
fulcrum.getDatabase().getDatabaseConnection("main");
fulcrum.getDatabase().getRedisConnection("cache");
```

## Default Argument Types

Fulcrum registers converters for:

- `String`
- `Integer`
- `Long`
- `Double`
- `UUID`
- Platform player class, through the active `FulcrumServer`

Use `String[].class` for a trailing argument that captures the rest of the command input.

## Practical Notes

- Bind SQL values with `?` parameters. Do not concatenate user input into SQL.
- Treat table names, column names, and raw `WHERE` fragments as trusted-only values unless you validate them yourself.
- Redis Pub/Sub messages should be considered external input if any other service can publish to the same Redis instance.
- Register commands during plugin enable/startup, then let Fulcrum unregister platform commands during `fulcrum.stop()`.
