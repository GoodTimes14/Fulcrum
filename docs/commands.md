# Command Handling

Fulcrum commands are described by a `CommandScheme` and implemented by extending `FulcrumCommand`. The same command definition can be wrapped by the Spigot, BungeeCord, Velocity, or terminal command register.

## Minimal Command

```java
public final class HelloCommand extends FulcrumCommand {

    public HelloCommand(Fulcrum fulcrum) {
        super(fulcrum);
    }

    @Override
    public String plugin() {
        return "example";
    }

    @Override
    public CommandScheme scheme() {
        return CommandScheme.builder()
                .label("hello")
                .description("Sends a greeting")
                .source(SourceType.ALL)
                .commandExecutor(this::execute)
                .build();
    }

    private void execute(ICommandContext context) {
        context.source().sendMessage("&aHello, " + context.source().getName() + "!");
    }
}
```

Register it after `fulcrum.start(this)`:

```java
fulcrum.getCommandManager().wrapCommand(new HelloCommand(fulcrum));
```

## Permissions

Add `.permission("node")` to the root command or any subcommand:

```java
CommandScheme.builder()
        .label("adminhello")
        .permission("example.adminhello")
        .source(SourceType.ALL)
        .commandExecutor(ctx -> ctx.source().sendMessage("&aAuthorized"))
        .build();
```

Fulcrum checks root and subcommand permissions before executing handlers. If the source lacks permission, it sends the configured no-permission message from `messages.yml`.

## Source Types

Restrict commands to players, console, or all sources:

```java
CommandScheme.builder()
        .label("consoleonly")
        .source(SourceType.CONSOLE)
        .commandExecutor(ctx -> ctx.source().sendMessage("Console command executed"))
        .build();
```

The active platform adapter provides the `FulcrumSource`, so handlers can use:

```java
ctx.source().getName();
ctx.source().getUniqueId();
ctx.source().sendMessage("&aDone");
ctx.source().hasPermission("example.node");
ctx.source().sourceType();
```

## Arguments

Use `NormalArgument` for a typed value:

```java
CommandScheme.builder()
        .label("pay")
        .argument(NormalArgument.builder()
                .name("player")
                .type(String.class)
                .required(true)
                .description("Target player name")
                .build())
        .argument(NormalArgument.builder()
                .name("amount")
                .type(Double.class)
                .required(true)
                .description("Amount to pay")
                .build())
        .commandExecutor(this::pay)
        .build();
```

Read parsed arguments from the context:

```java
private void pay(ICommandContext ctx) {
    String player = ctx.argument(0, String.class).orElseThrow();
    double amount = ctx.argument(1, Double.class).orElseThrow();

    ctx.source().sendMessage("&aPaid " + amount + " to " + player);
}
```

Supported default conversions include `String`, `Integer`, `Long`, `Double`, `UUID`, and the platform player class.

## Greedy Text Argument

Use `String[].class` to capture the rest of the input:

```java
CommandScheme.builder()
        .label("broadcast")
        .permission("example.broadcast")
        .argument(NormalArgument.builder()
                .name("message")
                .type(String[].class)
                .required(true)
                .description("Message to broadcast")
                .build())
        .commandExecutor(this::broadcast)
        .build();
```

```java
private void broadcast(ICommandContext ctx) {
    String message = ctx.argument(0, String[].class)
            .map(words -> String.join(" ", words))
            .orElse("");

    ctx.source().sendMessage("&7Broadcast: &f" + message);
}
```

## Grouped Arguments

Use `GroupedArgument` when only specific values are allowed. Fulcrum also uses these values for tab completion.

```java
CommandScheme.builder()
        .label("mode")
        .argument(GroupedArgument.builder()
                .name("mode")
                .type(String.class)
                .required(true)
                .values("easy", "normal", "hard")
                .description("Game mode")
                .build())
        .commandExecutor(this::setMode)
        .build();
```

```java
private void setMode(ICommandContext ctx) {
    String mode = ctx.argument(0, String.class).orElseThrow();
    ctx.source().sendMessage("&aMode set to " + mode);
}
```

## Subcommands

Subcommands are nested `CommandScheme` objects:

```java
@Override
public CommandScheme scheme() {
    return CommandScheme.builder()
            .label("economy")
            .permission("economy.use")
            .source(SourceType.ALL)
            .description("Economy commands")
            .subCommands(
                    CommandScheme.builder()
                            .label("balance")
                            .description("Shows your balance")
                            .commandExecutor(this::balance)
                            .build(),
                    CommandScheme.builder()
                            .label("set")
                            .permission("economy.admin")
                            .argument(NormalArgument.builder()
                                    .name("player")
                                    .type(String.class)
                                    .required(true)
                                    .build())
                            .argument(NormalArgument.builder()
                                    .name("amount")
                                    .type(Double.class)
                                    .required(true)
                                    .build())
                            .commandExecutor(this::setBalance)
                            .build())
            .build();
}
```

This gives you:

```text
/economy balance
/economy set <player> <amount>
```

The root permission `economy.use` applies before any subcommand is reached. The `set` subcommand also requires `economy.admin`.

## Custom Tab Completion

Add a `tabExecutor` when the default grouped/player suggestions are not enough:

```java
CommandScheme.builder()
        .label("warp")
        .argument(NormalArgument.builder()
                .name("name")
                .type(String.class)
                .required(true)
                .build())
        .tabExecutor(ctx -> List.of("spawn", "market", "arena"))
        .commandExecutor(this::warp)
        .build();
```

Fulcrum merges custom completions with built-in subcommand and argument suggestions.

## Practical Command With SQL

This command reads the sender's balance from the SQL examples in [Relational Database Handling](database-sql.md):

```java
public final class BalanceCommand extends FulcrumCommand {

    public BalanceCommand(Fulcrum fulcrum) {
        super(fulcrum);
    }

    @Override
    public String plugin() {
        return "economy";
    }

    @Override
    public CommandScheme scheme() {
        return CommandScheme.builder()
                .label("balance")
                .permission("economy.balance")
                .source(SourceType.ALL)
                .commandExecutor(this::execute)
                .build();
    }

    private void execute(ICommandContext ctx) {
        UUID uuid = ctx.source().getUniqueId();
        if (uuid == null) {
            ctx.source().sendMessage("&cOnly players have a balance.");
            return;
        }

        RelationalConnection connection = getFulcrum().getDatabase()
                .getDatabaseConnection("main")
                .orElseThrow();

        connection.asyncInteraction(db -> {
            try (ResultSet rs = db.query(
                    "SELECT balance FROM player_balance WHERE uuid = ?",
                    uuid.toString())) {

                double balance = rs.next() ? rs.getDouble("balance") : 0.0D;
                ctx.source().sendMessage("&aYour balance: " + balance);
            } catch (SQLException ex) {
                getFulcrum().getPlugin().getLogger().log(Level.SEVERE, "Unable to read balance", ex);
                ctx.source().sendMessage("&cUnable to read balance.");
            }
        });
    }
}
```

In a production Minecraft plugin, switch back to the platform main thread before sending messages if your platform requires it.

## Registering on Platform Startup

Spigot and BungeeCord examples normally register commands in `onEnable`:

```java
@Override
public void onEnable() {
    fulcrum.start(this);
    fulcrum.getCommandManager().wrapCommand(new BalanceCommand(fulcrum));
}
```

Velocity registers during `ProxyInitializeEvent`:

```java
@Subscribe
public void onProxyInitialization(ProxyInitializeEvent event) {
    fulcrum.start(this);
    fulcrum.getCommandManager().wrapCommand(new BalanceCommand(fulcrum));
}
```

Terminal commands use the same command objects and are registered before the input loop starts.
