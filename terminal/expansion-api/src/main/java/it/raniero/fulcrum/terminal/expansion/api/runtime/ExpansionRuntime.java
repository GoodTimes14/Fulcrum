package it.raniero.fulcrum.terminal.expansion.api.runtime;

/**
 * Runtime strategy capable of starting and stopping expansion handles.
 */
public interface ExpansionRuntime {

    /**
     * Gets the execution mode supported by this runtime.
     *
     * @return supported execution mode
     */
    ExpansionExecutionMode executionMode();

    /**
     * Starts the supplied expansion handle.
     *
     * @param handle expansion handle
     */
    void start(ExpansionHandle handle);

    /**
     * Stops the supplied expansion handle.
     *
     * @param handle expansion handle
     */
    void stop(ExpansionHandle handle);
}
