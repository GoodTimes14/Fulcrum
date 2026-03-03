package it.raniero.fulcrum.bungeecord.command.register;

import it.raniero.fulcrum.api.command.IFulcrumCommand;
import it.raniero.fulcrum.api.command.manager.ICommandRegister;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.bungeecord.FulcrumBungee;
import it.raniero.fulcrum.bungeecord.command.FulcrumCommandBungee;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;

@RequiredArgsConstructor
public class BungeeCordCommandRegister implements ICommandRegister {

    private final FulcrumBungee fulcrumBungee;

    private final Map<String, FulcrumCommandBungee> bungeeCommands = new HashMap<>();

    @Override
    public void registerCommand(IFulcrumCommand command) {
        FulcrumCommandBungee fulcrumCommand = (FulcrumCommandBungee) command;

        try {
            ProxyServer.getInstance().getPluginManager().registerCommand(fulcrumBungee, fulcrumCommand.getExecutor());
            bungeeCommands.put(fulcrumCommand.scheme().label(), fulcrumCommand);
        } catch (Exception e) {
            fulcrumBungee.getLogger().log(Level.SEVERE, "Error while registering command: ", e);
        }
    }

    @Override
    public void wrapCommand(IFulcrumCommand command) {
        FulcrumCommandBungee commandBungee = new FulcrumCommandBungee(fulcrumBungee.getFulcrum()) {
            @Override
            public String plugin() {
                return command.plugin();
            }

            @Override
            public CommandScheme scheme() {
                return command.scheme();
            }
        };

        registerCommand(commandBungee);
    }

    @Override
    public void unregisterCommand(String name) {

        FulcrumCommandBungee commandBungee = bungeeCommands.get(name);

        try {
            ProxyServer.getInstance().getPluginManager().unregisterCommand(commandBungee.getExecutor());
            bungeeCommands.remove(commandBungee.scheme().label());

        } catch (Exception e) {
            fulcrumBungee.getLogger().log(Level.SEVERE, "Error while unregistering command: ", e);
        }
    }

    @Override
    public void unregisterCommands() {

        for (FulcrumCommandBungee command : bungeeCommands.values()) {
            ProxyServer.getInstance().getPluginManager().unregisterCommand(command.getExecutor());
        }

        bungeeCommands.clear();
    }
}
