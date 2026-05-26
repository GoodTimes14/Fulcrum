# Relational Database Handling

Fulcrum stores relational connections in `IFulcrumDatabase` and exposes them as `RelationalConnection` objects. The default implementation uses HikariCP and MySQL.

## Configure a SQL Connection

In `databases.yml`:

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
```

After `fulcrum.start(plugin)`, enabled connections are registered automatically.

## Get a Connection

```java
RelationalConnection connection = fulcrum.getDatabase()
        .getDatabaseConnection("main")
        .orElseThrow(() -> new IllegalStateException("Database 'main' is not configured"));
```

Use `Optional` handling instead of assuming the connection exists. That lets your plugin fail cleanly when the server owner forgot to enable the connection.

## Create a Table

For migrations or startup setup, use a short interaction with auto-commit:

```java
public void createTables(Fulcrum fulcrum) {
    RelationalConnection connection = fulcrum.getDatabase()
            .getDatabaseConnection("main")
            .orElseThrow();

    try (RelationalInteraction db = connection.createInteraction(true)) {
        db.update("""
                CREATE TABLE IF NOT EXISTS player_balance (
                    uuid VARCHAR(36) PRIMARY KEY,
                    balance DOUBLE NOT NULL
                )
                """);
    } catch (SQLException ex) {
        fulcrum.getPlugin().getLogger().log(Level.SEVERE, "Unable to create tables", ex);
    }
}
```

## Insert or Update Values

Always bind values with `?` placeholders:

```java
public void setBalance(Fulcrum fulcrum, UUID uuid, double balance) {
    RelationalConnection connection = fulcrum.getDatabase()
            .getDatabaseConnection("main")
            .orElseThrow();

    try (RelationalInteraction db = connection.createInteraction(true)) {
        db.update("""
                INSERT INTO player_balance (uuid, balance)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE balance = VALUES(balance)
                """, uuid.toString(), balance);
    } catch (SQLException ex) {
        fulcrum.getPlugin().getLogger().log(Level.SEVERE, "Unable to save balance for " + uuid, ex);
    }
}
```

The SQL string stays stable and the values are sent separately to `PreparedStatement`.

## Read Values

```java
public double getBalance(Fulcrum fulcrum, UUID uuid) {
    RelationalConnection connection = fulcrum.getDatabase()
            .getDatabaseConnection("main")
            .orElseThrow();

    try (RelationalInteraction db = connection.createInteraction(true);
            ResultSet rs = db.query(
                    "SELECT balance FROM player_balance WHERE uuid = ?",
                    uuid.toString())) {

        if (rs.next()) {
            return rs.getDouble("balance");
        }
        return 0.0D;
    } catch (SQLException ex) {
        fulcrum.getPlugin().getLogger().log(Level.SEVERE, "Unable to read balance for " + uuid, ex);
        return 0.0D;
    }
}
```

## Use a Transaction

Use `createInteraction(false)` when multiple updates must commit or roll back together:

```java
public boolean transfer(Fulcrum fulcrum, UUID from, UUID to, double amount) {
    RelationalConnection connection = fulcrum.getDatabase()
            .getDatabaseConnection("main")
            .orElseThrow();

    try (RelationalInteraction db = connection.createInteraction(false)) {
        db.update(
                "UPDATE player_balance SET balance = balance - ? WHERE uuid = ?",
                amount,
                from.toString());
        db.update(
                "UPDATE player_balance SET balance = balance + ? WHERE uuid = ?",
                amount,
                to.toString());

        db.commit();
        return true;
    } catch (SQLException ex) {
        fulcrum.getPlugin().getLogger().log(Level.SEVERE, "Unable to transfer balance", ex);
        return false;
    }
}
```

For more complex code, call `rollback()` inside a catch block if you keep the interaction outside try-with-resources.

## Run Work Asynchronously

`RelationalConnection` has an internal executor for database work:

```java
public CompletableFuture<Void> saveBalanceAsync(Fulcrum fulcrum, UUID uuid, double balance) {
    RelationalConnection connection = fulcrum.getDatabase()
            .getDatabaseConnection("main")
            .orElseThrow();

    return connection.asyncInteraction(db -> {
        try {
            db.update("""
                    INSERT INTO player_balance (uuid, balance)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE balance = VALUES(balance)
                    """, uuid.toString(), balance);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    });
}
```

When using async methods from a Minecraft plugin, keep platform rules in mind: do database work off-thread, but switch back to the platform scheduler before touching APIs that must run on the main server thread.

## QueryBuilder and TableBuilder

Fulcrum includes simple SQL string builders:

```java
String sql = new QueryBuilder()
        .select("player_balance", "balance")
        .where(new QueryCondition("uuid = ?"))
        .build();

try (RelationalInteraction db = connection.createInteraction(true);
        ResultSet rs = db.query(sql, uuid.toString())) {
    // read result
}
```

Use these builders only with trusted table names, column names, and condition fragments. Bind values through `RelationalInteraction.query(sql, params...)` or `update(sql, params...)`.

Unsafe:

```java
String sql = new QueryBuilder()
        .select(playerInputTableName, "balance")
        .where(new QueryCondition("uuid = '" + playerInputUuid + "'"))
        .build();
```

Safe:

```java
String sql = new QueryBuilder()
        .select("player_balance", "balance")
        .where(new QueryCondition("uuid = ?"))
        .build();

ResultSet rs = db.query(sql, uuid.toString());
```

## Cleanup

Fulcrum closes registered connections during:

```java
fulcrum.stop();
```

If you manually unregister a connection:

```java
fulcrum.getDatabase().unregisterConnection(ConnectionType.SQL, "main");
```

