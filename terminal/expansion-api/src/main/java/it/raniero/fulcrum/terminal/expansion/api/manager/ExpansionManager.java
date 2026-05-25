package it.raniero.fulcrum.terminal.expansion.api.manager;

import it.raniero.fulcrum.terminal.expansion.api.descriptor.ExpansionDescriptor;
import it.raniero.fulcrum.terminal.expansion.api.runtime.ExpansionHandle;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

/**
 * Coordinates discovery and lifecycle changes for terminal expansions.
 */
public interface ExpansionManager {

    /**
     * Discovers expansion descriptors in the provided folder.
     *
     * @param expansionFolder folder containing expansion artifacts
     * @return discovered expansion descriptors
     * @throws IOException when the folder cannot be inspected
     */
    Collection<ExpansionDescriptor> discover(Path expansionFolder) throws IOException;

    /**
     * Loads the expansion identified by the descriptor.
     *
     * @param descriptor descriptor to load
     * @return loaded expansion handle
     * @throws IOException when the expansion cannot be loaded
     */
    ExpansionHandle load(ExpansionDescriptor descriptor) throws IOException;

    /**
     * Enables the expansion represented by the handle.
     *
     * @param handle loaded expansion handle
     */
    void enable(ExpansionHandle handle);

    /**
     * Disables the expansion represented by the handle.
     *
     * @param handle loaded expansion handle
     */
    void disable(ExpansionHandle handle);

    /**
     * Gets all loaded expansion handles.
     *
     * @return loaded expansions
     */
    Collection<ExpansionHandle> loadedExpansions();

    /**
     * Gets a loaded expansion by name.
     *
     * @param name expansion name
     * @return loaded expansion when present
     */
    Optional<ExpansionHandle> expansion(String name);
}
