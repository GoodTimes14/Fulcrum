package it.raniero.fulcrum.api.command.manager;

import it.raniero.fulcrum.api.command.IFulcrumCommand;
import it.raniero.fulcrum.api.command.scheme.CommandScheme;

/**
 * Coordinates registration and lifecycle operations for Fulcrum commands.
 */
public interface ICommandManager {

    /**
     * Registers a command.
     *
     * @param command command to register
     */
    void registerCommand(IFulcrumCommand command);

    /**
     * Wraps a command with optional additional sub-command schemes.
     *
     * @param command command to wrap
     * @param additionalSubCommands extra sub-command schemes to expose
     */
    void wrapCommand(IFulcrumCommand command, CommandScheme... additionalSubCommands);

    /**
     * Registers multiple commands in order.
     *
     * @param commands commands to register
     */
    default void registerCommands(IFulcrumCommand... commands) {
        for (IFulcrumCommand command : commands) {
            registerCommand(command);
        }
    }

    /**
     * Unregisters a command by name.
     *
     * @param name command name to unregister
     */
    void unregisterCommand(String name);

    /**
     * Unregisters every command managed by this manager.
     */
    void unregisterAllCommands();
}
