package it.raniero.fulcrum.terminal.expansion.api.descriptor;

/**
 * Dependency relationship declared by an expansion descriptor.
 */
public interface ExpansionDependency {

    /**
     * Gets the target expansion name.
     *
     * @return target expansion name
     */
    String name();

    /**
     * Gets the dependency relationship type.
     *
     * @return dependency type
     */
    ExpansionDependencyType type();
}
