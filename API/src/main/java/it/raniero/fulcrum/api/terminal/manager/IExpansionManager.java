package it.raniero.fulcrum.api.terminal.manager;

import java.nio.file.Path;

/**
 * Discovers and loads terminal expansions from the filesystem.
 */
public interface IExpansionManager {

    /**
     * Searches the provided folder for expansions that can be loaded.
     *
     * @param expansionFolder folder containing expansion artifacts
     */
    void searchExpansions(Path expansionFolder);
}
