package it.raniero.fulcrum.command.manager;

import it.raniero.fulcrum.command.IFulcrumCommand;

public interface ICommandRegister {

    void registerCommand(IFulcrumCommand command);

    void unregisterCommand(String name);

    void unregisterCommands();
}
