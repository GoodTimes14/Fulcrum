package it.raniero.fulcrum.api.conversion;

import it.raniero.fulcrum.api.command.context.ICommandContext;

/**
 * Manages converters used to parse command arguments.
 */
public interface IConversionManager {

    /**
     * Initializes the default converters.
     */
    void init();

    /**
     * Registers a converter for the supplied type.
     *
     * @param type type handled by the converter
     * @param converter converter implementation
     * @param <T> converted value type
     */
    <T> void registerConverter(Class<T> type, IConverter<T> converter);

    /**
     * Converts a parameter and adds the result to a command context.
     *
     * @param type requested argument type
     * @param parameter raw argument text
     * @param context command context to receive the converted value
     * @return {@code true} when conversion succeeded
     */
    boolean convertAndAddArgument(Class<?> type, String parameter, ICommandContext context);

    /**
     * Converts a parameter to the requested type.
     *
     * @param type requested argument type
     * @param parameter raw argument text
     * @return converted value, or {@code null} when unavailable
     */
    Object convertArgument(Class<?> type, String parameter);

    /**
     * Gets the converter registered for an argument type.
     *
     * @param type argument type
     * @param <T> converted value type
     * @return matching converter, or {@code null} when none is registered
     */
    <T> IConverter<T> getConverterForArgumentType(Class<T> type);
}
