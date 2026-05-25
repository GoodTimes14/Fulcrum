package it.raniero.fulcrum.terminal.expansion.api;

import it.raniero.fulcrum.api.FulcrumAPI;
import it.raniero.fulcrum.terminal.expansion.api.descriptor.ExpansionDescriptor;
import it.raniero.fulcrum.terminal.expansion.api.runtime.ExpansionExecutionMode;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Context exposed to an expansion by the terminal host.
 */
public interface ExpansionContext {

    /**
     * Gets the Fulcrum API instance owned by the terminal.
     *
     * @return active Fulcrum API instance
     */
    FulcrumAPI fulcrum();

    /**
     * Gets the descriptor used to load this expansion.
     *
     * @return expansion descriptor
     */
    ExpansionDescriptor descriptor();

    /**
     * Gets the isolated data folder assigned to this expansion.
     *
     * @return expansion data folder
     */
    Path dataFolder();

    /**
     * Gets the logger assigned to this expansion.
     *
     * @return expansion logger
     */
    Logger logger();

    /**
     * Gets the execution mode selected for this expansion.
     *
     * @return execution mode
     */
    ExpansionExecutionMode executionMode();
}
