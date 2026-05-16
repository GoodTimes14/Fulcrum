package it.raniero.fulcrum.api.command.context;

import it.raniero.fulcrum.api.command.context.result.ContextResult;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.util.Optional;

/**
 * Holds state produced while parsing and executing a command invocation.
 */
public interface ICommandContext {

    /**
     * Gets the source that invoked the command.
     *
     * @return command source
     */
    FulcrumSource source();

    /**
     * Gets the raw parameters supplied to the command.
     *
     * @return original command parameters
     */
    String[] originalParameters();

    /**
     * Gets the command label used for the invocation.
     *
     * @return command label
     */
    String label();

    /**
     * Gets the server adapter that received the command.
     *
     * @return server adapter
     */
    FulcrumServer server();

    /**
     * Gets a parsed argument by index and expected type.
     *
     * @param index argument index
     * @param type expected argument type
     * @param <T> argument type
     * @return parsed argument when present and assignable
     */
    <T> Optional<T> argument(int index, Class<T> type);

    /**
     * Adds a parsed argument to the context.
     *
     * @param object parsed argument value
     */
    void addArgument(Object object);

    /**
     * Gets the current context result.
     *
     * @return context result
     */
    ContextResult result();

    /**
     * Updates the current context result.
     *
     * @param result new context result
     */
    void result(ContextResult result);
}
