package it.raniero.fulcrum.terminal.expansion.api.runtime;

/**
 * Lifecycle state of an expansion known by the terminal host.
 */
public enum ExpansionState {

    /**
     * The expansion has been found but not loaded.
     */
    DISCOVERED,

    /**
     * The expansion has been loaded but not enabled.
     */
    LOADED,

    /**
     * The expansion is enabled.
     */
    ENABLED,

    /**
     * The expansion is disabled.
     */
    DISABLED,

    /**
     * The expansion failed during discovery, load, enable, or disable.
     */
    FAILED
}
