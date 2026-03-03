package it.raniero.fulcrum.api.command.manager;

import it.raniero.fulcrum.api.command.IFulcrumCommand;

public interface ICommandRegister {

    void registerCommand(IFulcrumCommand command);

    void wrapCommand(IFulcrumCommand command);

    void unregisterCommand(String name);

    void unregisterCommands();
}
