package it.raniero.fulcrum.command;

import it.raniero.fulcrum.command.scheme.CommandScheme;

public interface IFulcrumCommand {


    void defaultCommand(CommandScheme scheme);

    void subCommand(CommandScheme scheme);

    void executeCommand();

}
