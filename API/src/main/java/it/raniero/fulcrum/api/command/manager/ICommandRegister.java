package it.raniero.fulcrum.api.command.manager;

import it.raniero.fulcrum.api.command.IFulcrumCommand;

/**
 * Registers and unregisters platform command wrappers.
 */
public interface ICommandRegister {

    /**
     * Registers a Fulcrum command with the platform.
     *
     * @param command command to register
     */
    void registerCommand(IFulcrumCommand command);

    /**
     * Wraps a Fulcrum command in the platform-specific command representation.
     *
     * @param command command to wrap
     */
    void wrapCommand(IFulcrumCommand command);

    /**
     * Unregisters a command by name.
     *
     * @param name command name to unregister
     */
    void unregisterCommand(String name);

    /**
     * Unregisters all commands owned by this register.
     */
    void unregisterCommands();
}
