package it.raniero.fulcrum.api.command.manager;

import it.raniero.fulcrum.api.command.IFulcrumCommand;

public interface ICommandManager {

    void registerCommand(IFulcrumCommand command);

    default void registerCommands(IFulcrumCommand... commands) {
        for (IFulcrumCommand command : commands) {
            registerCommand(command);
        }
    }

    void unregisterCommand(String name);

    void unregisterAllCommands();
}
