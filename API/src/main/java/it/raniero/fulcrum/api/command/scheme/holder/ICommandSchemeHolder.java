package it.raniero.fulcrum.api.command.scheme.holder;

import it.raniero.fulcrum.api.command.scheme.CommandScheme;

/**
 * Exposes the root command scheme for an object.
 */
public interface ICommandSchemeHolder {

    /**
     * Gets the root command scheme.
     *
     * @return command scheme
     */
    CommandScheme scheme();
}
