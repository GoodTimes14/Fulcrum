package it.raniero.fulcrum.terminal.expansion.api.loader;

import java.nio.file.Path;

/**
 * Filesystem artifact that can be inspected as a terminal expansion.
 *
 * @param path artifact path
 */
public record ExpansionArtifact(Path path) {}
