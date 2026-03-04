package it.raniero.fulcrum.api.command;

import it.raniero.fulcrum.api.command.context.result.ContextResult;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;
import it.raniero.fulcrum.api.command.scheme.holder.ICommandSchemeHolder;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.util.List;

public interface IFulcrumCommand extends ICommandSchemeHolder {

    void registerScheme(CommandScheme scheme);

    void executeCommand(FulcrumServer server, FulcrumSource sender, String label, String[] args);

    void sendCommandUsage(FulcrumSource source, String label, CommandScheme scheme);

    void sendCommandHelp(
            FulcrumSource source, String label, CommandScheme scheme, ContextResult result, boolean sendPreamble);

    List<String> executeTabCompletion(FulcrumServer server, FulcrumSource sender, String label, String[] args);

    String plugin();
}
