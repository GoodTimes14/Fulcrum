package it.raniero.fulcrum.terminal.expansion.api.descriptor;

import it.raniero.fulcrum.terminal.expansion.api.runtime.ExpansionExecutionMode;
import java.util.List;

/**
 * Metadata read from an expansion artifact before it is loaded.
 */
public interface ExpansionDescriptor {

    /**
     * Gets the unique expansion name.
     *
     * @return expansion name
     */
    String name();

    /**
     * Gets the expansion version.
     *
     * @return expansion version
     */
    String version();

    /**
     * Gets the fully qualified entry point class name.
     *
     * @return main class name
     */
    String mainClass();

    /**
     * Gets a short human-readable description.
     *
     * @return expansion description
     */
    String description();

    /**
     * Gets the authors declared by the expansion.
     *
     * @return declared authors
     */
    List<String> authors();

    /**
     * Gets the dependency relationships declared by the expansion.
     *
     * @return declared dependencies
     */
    List<ExpansionDependency> dependencies();

    /**
     * Gets the load phase requested by the expansion.
     *
     * @return requested load phase
     */
    ExpansionLoadPhase loadPhase();

    /**
     * Gets the execution strategy requested by the expansion.
     *
     * @return requested execution mode
     */
    ExpansionExecutionMode executionMode();
}
