package it.raniero.fulcrum.conversion;

public interface IConverter<T> {


    Class<T> type();

    T convert(String string);

    boolean canConvert(String string);


}
