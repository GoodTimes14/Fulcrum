package it.raniero.fulcrum.velocity.command.register;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import it.raniero.fulcrum.command.IFulcrumCommand;
import it.raniero.fulcrum.command.manager.ICommandRegister;
import it.raniero.fulcrum.server.FulcrumServer;
import it.raniero.fulcrum.velocity.command.executor.FulcrumVelocityExecutor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VelocityCommandRegister implements ICommandRegister {

    private final FulcrumServer fulcrumServer;

    private final ProxyServer server;

    private final Logger logger;

    private final Map<String, IFulcrumCommand> velocityCommands = new HashMap<>();

    @Override
    public void registerCommand(IFulcrumCommand command) {

        CommandManager commandManager = server.getCommandManager();
        PluginContainer pluginContainer =
                server.getPluginManager().getPlugin(command.plugin()).orElse(null);
        if (pluginContainer == null) {
            logger.log(
                    Level.SEVERE, "Can't register command: " + command.scheme().label() + ", can't find plugin");
            return;
        }

        CommandMeta meta = commandManager
                .metaBuilder(command.scheme().label())
                .aliases(
                        command.scheme().aliases() != null
                                ? command.scheme().aliases().toArray(new String[0])
                                : new String[0])
                .plugin(pluginContainer.getInstance().orElse(null))
                .build();

        server.getCommandManager().register(meta, new FulcrumVelocityExecutor(fulcrumServer, command));
        velocityCommands.put(command.scheme().label(), command);
    }

    @Override
    public void unregisterCommand(String name) {
        if (velocityCommands.containsKey(name)) {
            server.getCommandManager().unregister(name);
            velocityCommands.remove(name);
        }
    }

    @Override
    public void unregisterCommands() {
        for (String label : velocityCommands.keySet()) {
            server.getCommandManager().unregister(label);
        }
    }
}
