package it.raniero.fulcrum.api.command.manager;

import it.raniero.fulcrum.api.command.IFulcrumCommand;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;

public interface ICommandManager {

    void registerCommand(IFulcrumCommand command);

    void wrapCommand(IFulcrumCommand command, CommandScheme... additionalSubCommands);

    default void registerCommands(IFulcrumCommand... commands) {
        for (IFulcrumCommand command : commands) {
            registerCommand(command);
        }
    }

    void unregisterCommand(String name);

    void unregisterAllCommands();
}
