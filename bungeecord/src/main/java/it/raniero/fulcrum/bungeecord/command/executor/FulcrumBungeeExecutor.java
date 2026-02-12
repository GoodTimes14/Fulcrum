package it.raniero.fulcrum.bungeecord.command.executor;

import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.bungeecord.server.source.FulcrumBungeeCordSource;
import it.raniero.fulcrum.command.FulcrumCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class FulcrumBungeeExecutor extends Command implements TabExecutor {

    private final FulcrumCommand fulcrumCommand;

    private final FulcrumServer fulcrumServer;

    public FulcrumBungeeExecutor(FulcrumCommand fulcrumCommand, FulcrumServer fulcrumServer) {
        super(
                fulcrumCommand.scheme().label(),
                "",
                fulcrumCommand.scheme().aliases() == null
                        ? new String[0]
                        : fulcrumCommand.scheme().aliases().toArray(new String[0]));
        this.fulcrumServer = fulcrumServer;
        this.fulcrumCommand = fulcrumCommand;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        fulcrumCommand.executeCommand(
                fulcrumServer,
                new FulcrumBungeeCordSource(sender),
                fulcrumCommand.scheme().label(),
                args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return fulcrumCommand.executeTabCompletion(
                fulcrumServer,
                new FulcrumBungeeCordSource(sender),
                fulcrumCommand.scheme().label(),
                args);
    }
}
