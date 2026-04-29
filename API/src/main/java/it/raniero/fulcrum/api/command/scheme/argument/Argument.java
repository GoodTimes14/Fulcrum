package it.raniero.fulcrum.api.command.scheme.argument;

import it.raniero.fulcrum.api.command.context.ICommandContext;
import it.raniero.fulcrum.api.conversion.IConversionManager;
import it.raniero.fulcrum.api.server.FulcrumServer;

/**
 * Defines a single argument in a command scheme.
 */
public interface Argument {

    /**
     * Gets the argument name.
     *
     * @return argument name
     */
    String name();

    /**
     * Gets the argument description.
     *
     * @return argument description
     */
    String description();

    /**
     * Checks whether this argument is required.
     *
     * @return {@code true} when the argument is required
     */
    boolean required();

    /**
     * Parses and stores this argument in the command context.
     *
     * @param index argument index in the command input
     * @param conversionManager conversion manager used to parse values
     * @param server server adapter receiving the command
     * @param context command context being compiled
     */
    void compileArgument(
            int index, IConversionManager conversionManager, FulcrumServer server, ICommandContext context);

    /**
     * Gets the display form used in command usage text.
     *
     * @return display form
     */
    String display();
}
