package it.raniero.fulcrum.command.common;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.ICommandContext;
import it.raniero.fulcrum.api.command.context.source.SourceType;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
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

    @Override
    public String plugin() {
        return "fulcrum";
    }

    @Override
    public CommandScheme scheme() {
        return CommandScheme.builder()
                .label("fulcrum")
                .source(SourceType.ALL)
                .description("Main Fulcrum command")
                .subCommand(CommandScheme.builder()
                        .label("connections")
                        .commandExecutor(this::listConnections)
                        .description("List fulcrum active connections")
                        .build())
                .build();
    }
}
