package it.raniero.fulcrum.spigot.command.executor;

import it.raniero.fulcrum.command.FulcrumCommand;
import it.raniero.fulcrum.server.FulcrumServer;
import it.raniero.fulcrum.spigot.command.source.FulcrumSpigotSource;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class FulcrumCommandExecutor extends BukkitCommand {

    private final FulcrumServer fulcrumServer;
    private final FulcrumCommand fulcrumCommand;

    public FulcrumCommandExecutor(FulcrumCommand fulcrumCommand, FulcrumServer fulcrumServer) {
        super(
                fulcrumCommand.getCommandScheme().label(),
                fulcrumCommand.getCommandScheme().description(),
                "",
                fulcrumCommand.getCommandScheme().aliases() == null
                        ? new ArrayList<>()
                        : fulcrumCommand.getCommandScheme().aliases());

        this.fulcrumCommand = fulcrumCommand;
        this.fulcrumServer = fulcrumServer;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        fulcrumCommand.executeCommand(fulcrumServer, new FulcrumSpigotSource(sender), label, args);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException {
        return fulcrumCommand.executeTabCompletion(fulcrumServer, new FulcrumSpigotSource(sender), label, args);
    }
}
