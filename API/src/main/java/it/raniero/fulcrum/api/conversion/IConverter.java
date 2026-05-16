package it.raniero.fulcrum.api.conversion;

/**
 * Converts command argument text into a strongly typed value.
 *
 * @param <T> converted value type
 */
public interface IConverter<T> {

    /**
     * Gets the type produced by this converter.
     *
     * @return converted value type
     */
    Class<T> type();

    /**
     * Converts text into the target value type.
     *
     * @param string raw argument text
     * @return converted value
     */
    T convert(String string);

    /**
     * Checks whether the provided text can be converted.
     *
     * @param string raw argument text
     * @return {@code true} when conversion can succeed
     */
    boolean canConvert(String string);
}
