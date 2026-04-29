package it.raniero.fulcrum.api.terminal;

import it.raniero.fulcrum.api.FulcrumAPI;

/**
 * Represents an extension loaded by the Fulcrum terminal runtime.
 */
public interface TerminalExpansion {

    /**
     * Starts the expansion with access to the Fulcrum API.
     *
     * @param fulcrum active Fulcrum API instance
     */
    void start(FulcrumAPI fulcrum);

    /**
     * Stops the expansion and releases any resources it owns.
     */
    void stop();
}
