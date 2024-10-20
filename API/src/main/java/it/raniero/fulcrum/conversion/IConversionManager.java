package it.raniero.fulcrum.conversion;

public interface IConversionManager {


    <T> void registerConverter(Class<T> type, IConverter<T> converter);

    <T> IConverter<T> getConverterForArgumentType(Class<T> type);


}
