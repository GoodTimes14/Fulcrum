package it.raniero.fulcrum.command;

import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.scheme.CommandScheme;
import it.raniero.fulcrum.server.FulcrumServer;

public interface IFulcrumCommand {


    void executeCommand(FulcrumServer server, FulcrumSource sender, String[] args);


}
