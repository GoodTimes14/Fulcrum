package it.raniero.fulcrum.spigot.command.impl;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.context.source.SourceType;
import it.raniero.fulcrum.command.scheme.CommandScheme;
import it.raniero.fulcrum.command.scheme.argument.impl.NormalArgument;
import it.raniero.fulcrum.database.relational.RelationalConnection;
import it.raniero.fulcrum.spigot.command.FulcrumCommandSpigot;
import it.raniero.fulcrum.spigot.command.source.FulcrumSpigotSource;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;

public class MainCommand extends FulcrumCommandSpigot {

    public MainCommand(Fulcrum fulcrum) {
        super(fulcrum);
    }

    @Override
    public String plugin() {
        return "fulcrum";
    }

    public void listConnections(ICommandContext context) {


        StringBuilder connectionsMessage = new StringBuilder();

        Map<String,RelationalConnection> connections = getFulcrum().getDatabase().getDatabaseConnections();

        connectionsMessage.append(ChatColor.GRAY + "There are " +
                ChatColor.RED + connections.size() +
                ChatColor.GRAY + " connections available:\n");
        long timestamp = System.currentTimeMillis();
        for (Map.Entry<String, RelationalConnection> entry : getFulcrum().getDatabase().getDatabaseConnections().entrySet()) {
            long delta = timestamp - entry.getValue().getLastActionTime();
            connectionsMessage
                    .append("&7" + entry.getKey() +
                            " &8- &7Last Action &e" + delta + "ms").append("\n");
        }


        context.source().sendMessage(connectionsMessage.toString());

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
