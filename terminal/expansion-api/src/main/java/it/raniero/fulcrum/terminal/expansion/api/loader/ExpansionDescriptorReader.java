package it.raniero.fulcrum.terminal.expansion.api.loader;

import it.raniero.fulcrum.terminal.expansion.api.descriptor.ExpansionDescriptor;
import java.io.IOException;

/**
 * Reads expansion metadata from an artifact.
 */
public interface ExpansionDescriptorReader {

    /**
     * Reads the descriptor for the provided artifact.
     *
     * @param artifact artifact to inspect
     * @return expansion descriptor
     * @throws IOException when the artifact cannot be read
     */
    ExpansionDescriptor readDescriptor(ExpansionArtifact artifact) throws IOException;
}
