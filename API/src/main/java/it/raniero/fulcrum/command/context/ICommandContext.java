package it.raniero.fulcrum.command.context;

import it.raniero.fulcrum.command.context.result.ContextResult;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import java.util.Optional;

public interface ICommandContext {

    FulcrumSource source();

    String[] originalParameters();

    <T> Optional<T> argument(int index, Class<T> type);

    void addArgument(Object object);

    ContextResult result();

    void setResult(ContextResult result);
}
