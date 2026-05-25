package it.raniero.fulcrum.terminal.expansion.api;

/**
 * Entry point implemented by a terminal expansion.
 */
public interface TerminalExpansion {

    /**
     * Called when the expansion has been created and its descriptor is available.
     *
     * @param context immutable runtime context supplied by the terminal host
     */
    void onLoad(ExpansionContext context);

    /**
     * Called when the expansion should start accepting work.
     *
     * @param context immutable runtime context supplied by the terminal host
     */
    void onEnable(ExpansionContext context);

    /**
     * Called when the expansion should release its resources.
     *
     * @param context immutable runtime context supplied by the terminal host
     */
    void onDisable(ExpansionContext context);
}
