package it.raniero.fulcrum.command.common;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.ICommandContext;
import it.raniero.fulcrum.api.command.context.source.SourceType;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.command.scheme.argument.impl.GroupedArgument;
import it.raniero.fulcrum.api.database.relational.RelationalConnection;
import it.raniero.fulcrum.command.FulcrumCommand;
import java.util.Map;

public abstract class FulcrumMainCommand extends FulcrumCommand {

    public FulcrumMainCommand(Fulcrum fulcrum) {
        super(fulcrum);
    }

    public void listConnections(ICommandContext context) {

        StringBuilder connectionsMessage = new StringBuilder();

        Map<String, RelationalConnection> connections =
                getFulcrum().getDatabase().getDatabaseConnections();

        connectionsMessage.append("§7There are §c" + connections.size() + "§7 connections available:\n");
        long timestamp = System.currentTimeMillis();
        for (Map.Entry<String, RelationalConnection> entry :
                getFulcrum().getDatabase().getDatabaseConnections().entrySet()) {
            long delta = timestamp - entry.getValue().getLastActionTime();
            String lastAction = entry.getValue().getLastActionTime() == -1 ? "Never used" : delta + "ms";

            boolean enabled = entry.getValue().properties().isEnabled();
            connectionsMessage
                    .append(enabled ? "&a" : "&c")
                    .append(entry.getKey())
                    .append(" &8- &7Last Action &e")
                    .append(lastAction)
                    .append("\n");
        }

        context.source().sendMessage(connectionsMessage.toString());
    }

    public void reload(ICommandContext context) {
        getFulcrum().getMainConfig().reload();
        context.source().sendMessage("&aConfig reloaded!");
    }

    public void testCommand(ICommandContext context) {}

    @Override
    public String plugin() {
        return "fulcrum";
    }

    @Override
    public CommandScheme scheme() {
        return CommandScheme.builder()
                .label("fulcrum")
                .permission("fulcrum.admin")
                .source(SourceType.ALL)
                .description("Main Fulcrum command")
                .subCommands(
                        CommandScheme.builder()
                                .label("connections")
                                .commandExecutor(this::listConnections)
                                .description("List fulcrum active connections")
                                .build(),
                        CommandScheme.builder()
                                .label("reload")
                                .commandExecutor(this::reload)
                                .build(),
                        CommandScheme.builder()
                                .label("test")
                                .subCommand(CommandScheme.builder()
                                        .label("add")
                                        .description("test order")
                                        .subCommand(CommandScheme.builder()
                                                .label("sium")
                                                .description("test order")
                                                .commandExecutor(this::testCommand)
                                                .build())
                                        .argument(GroupedArgument.builder()
                                                .name("cazzolong")
                                                .type(String.class)
                                                .values((Object[]) new String[] {"Ciao", "Prova", "Test"})
                                                .build())
                                        .build())
                                .description("List fulcrum active connections")
                                .build())
                .build();
    }
}
