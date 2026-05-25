package it.raniero.fulcrum.terminal.expansion.api.descriptor;

/**
 * Phase in which an expansion asks to be loaded.
 */
public enum ExpansionLoadPhase {

    /**
     * Load during terminal startup.
     */
    STARTUP,

    /**
     * Load after the terminal host has completed startup.
     */
    RUNTIME
}
