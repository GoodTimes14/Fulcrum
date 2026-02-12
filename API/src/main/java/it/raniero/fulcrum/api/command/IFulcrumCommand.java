package it.raniero.fulcrum.api.command;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.util.List;

public interface IFulcrumCommand {

    void registerScheme(CommandScheme scheme);

    void executeCommand(FulcrumServer server, FulcrumSource sender, String label, String[] args);

    List<String> executeTabCompletion(FulcrumServer server, FulcrumSource sender, String label, String[] args);

    String plugin();

    CommandScheme scheme();
}
