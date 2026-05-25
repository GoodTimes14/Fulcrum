package it.raniero.fulcrum.terminal.expansion.api.runtime;

/**
 * Strategy requested by an expansion for executing its lifecycle and work.
 */
public enum ExpansionExecutionMode {

    /**
     * Execute immediately on the terminal main thread.
     */
    INSTANT,

    /**
     * Execute on a thread dedicated to this expansion.
     */
    DEDICATED_THREAD,

    /**
     * Execute in a child process whose standard input, output, and error streams are inherited from the terminal.
     */
    SUBPROCESS
}
