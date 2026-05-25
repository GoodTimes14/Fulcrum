package it.raniero.fulcrum.terminal.expansion.api.runtime;

import it.raniero.fulcrum.terminal.expansion.api.descriptor.ExpansionDescriptor;

/**
 * Runtime reference to an expansion known by the terminal host.
 */
public interface ExpansionHandle {

    /**
     * Gets the descriptor used to load this expansion.
     *
     * @return expansion descriptor
     */
    ExpansionDescriptor descriptor();

    /**
     * Gets the current lifecycle state.
     *
     * @return expansion state
     */
    ExpansionState state();

    /**
     * Gets the execution mode selected for this expansion.
     *
     * @return selected execution mode
     */
    ExpansionExecutionMode executionMode();
}
