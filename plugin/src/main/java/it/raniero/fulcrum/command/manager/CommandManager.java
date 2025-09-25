package it.raniero.fulcrum.command.manager;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.IFulcrumCommand;
import it.raniero.fulcrum.command.exception.FulcrumCommandException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandManager implements ICommandManager {

    private final Fulcrum fulcrum;

    private final Map<String, IFulcrumCommand> registeredCommands = new ConcurrentHashMap<>();

    @Override
    public void registerCommand(IFulcrumCommand command) {
        if (registeredCommands.containsKey(command.scheme().label())) {
            throw new FulcrumCommandException(
                    command, "Command \"" + command.scheme().label() + "\" is already registered!");
        }

        registeredCommands.put(command.scheme().label(), command);
        fulcrum.getPlugin().getCommmandRegister().registerCommand(command);
    }

    @Override
    public void unregisterCommand(String name) {
        IFulcrumCommand command = registeredCommands.remove(name);
        if (command != null) {
            fulcrum.getPlugin().getCommmandRegister().unregisterCommand(name);
        }
    }

    @Override
    public void unregisterAllCommands() {
        for (IFulcrumCommand command : registeredCommands.values()) {
            fulcrum.getPlugin()
                    .getCommmandRegister()
                    .unregisterCommand(command.scheme().label());
        }

        registeredCommands.clear();
    }
}
