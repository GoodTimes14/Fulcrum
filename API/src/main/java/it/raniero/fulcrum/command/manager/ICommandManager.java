package it.raniero.fulcrum.command.manager;

import it.raniero.fulcrum.command.IFulcrumCommand;

public interface ICommandManager {

    void registerCommand(IFulcrumCommand command);

    void unregisterCommand();

    void unregisterAllCommands();
}
