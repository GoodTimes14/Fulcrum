package it.raniero.fulcrum.terminal.expansion.api.descriptor;

/**
 * Relationship types supported by an expansion descriptor.
 */
public enum ExpansionDependencyType {

    /**
     * The dependency must be loaded before this expansion can be enabled.
     */
    REQUIRED,

    /**
     * The dependency may be loaded before this expansion when present.
     */
    OPTIONAL,

    /**
     * This expansion should be loaded before the named target when possible.
     */
    LOAD_BEFORE
}
