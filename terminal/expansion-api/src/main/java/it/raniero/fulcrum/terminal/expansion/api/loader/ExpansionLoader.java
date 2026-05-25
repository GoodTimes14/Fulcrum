package it.raniero.fulcrum.terminal.expansion.api.loader;

import it.raniero.fulcrum.terminal.expansion.api.runtime.ExpansionHandle;
import java.io.IOException;

/**
 * Loads expansion artifacts into runtime handles.
 */
public interface ExpansionLoader {

    /**
     * Loads an expansion artifact.
     *
     * @param artifact artifact to load
     * @return loaded expansion handle
     * @throws IOException when the artifact cannot be loaded
     */
    ExpansionHandle load(ExpansionArtifact artifact) throws IOException;

    /**
     * Unloads an expansion handle.
     *
     * @param handle loaded expansion handle
     * @throws IOException when the handle cannot be unloaded
     */
    void unload(ExpansionHandle handle) throws IOException;
}
