package it.raniero.fulcrum.api.command.context;

import it.raniero.fulcrum.api.command.context.result.ContextResult;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.util.Optional;

public interface ICommandContext {

    FulcrumSource source();

    String[] originalParameters();

    String label();

    FulcrumServer server();

    <T> Optional<T> argument(int index, Class<T> type);

    void addArgument(Object object);

    ContextResult result();

    void result(ContextResult result);
}
