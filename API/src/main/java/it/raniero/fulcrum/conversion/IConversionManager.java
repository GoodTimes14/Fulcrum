package it.raniero.fulcrum.conversion;

import it.raniero.fulcrum.command.context.ICommandContext;

public interface IConversionManager {

    void init();

    <T> void registerConverter(Class<T> type, IConverter<T> converter);

    boolean convertAndAddArgument(Class<?> type, String parameter, ICommandContext context);

    Object convertArgument(Class<?> type, String parameter);

    <T> IConverter<T> getConverterForArgumentType(Class<T> type);
}
