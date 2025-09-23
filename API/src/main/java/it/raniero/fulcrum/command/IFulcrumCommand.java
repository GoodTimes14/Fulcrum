package it.raniero.fulcrum.command;

import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.scheme.CommandScheme;
import it.raniero.fulcrum.server.FulcrumServer;
import java.util.List;

public interface IFulcrumCommand {

    void registerScheme(CommandScheme scheme);

    void executeCommand(FulcrumServer server, FulcrumSource sender, String label, String[] args);

    List<String> executeTabCompletion(FulcrumServer server, FulcrumSource sender, String label, String[] args);

    CommandScheme scheme();
}
