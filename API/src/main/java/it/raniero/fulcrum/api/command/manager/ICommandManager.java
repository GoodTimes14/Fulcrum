package it.raniero.fulcrum.api.command.manager;

import it.raniero.fulcrum.api.command.IFulcrumCommand;

public interface ICommandManager {

    void registerCommand(IFulcrumCommand command);

    void unregisterCommand(String name);

    void unregisterAllCommands();
}
