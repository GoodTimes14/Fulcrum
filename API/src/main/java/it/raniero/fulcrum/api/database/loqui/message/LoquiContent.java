package it.raniero.fulcrum.api.database.loqui.message;

import it.raniero.fulcrum.api.database.loqui.exception.LoquiDecoderException;
import java.math.BigDecimal;
import java.util.Map;

public class LoquiContent {

    private final Map<String, Object> rawInput;

    public LoquiContent(Map<String, Object> rawInput) {
        this.rawInput = rawInput;
    }

    public Integer getInt(String key, int def) {

        long value = toLongExact(get(key, Number.class, def));
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new LoquiDecoderException("Numeric value is outside its boundaries: " + key);
        }

        return Math.toIntExact(value);
    }

    public Integer getInt(String key) {
        return getInt(key, 0);
    }

    public String getString(String key, String def) {
        return get(key, String.class, def);
    }

    public String getString(String key) {
        return get(key, String.class, null);
    }

    public Boolean getBoolean(String key, Boolean def) {
        return get(key, Boolean.class, def);
    }

    public Boolean getBoolean(String key) {
        return get(key, Boolean.class, null);
    }

    public Long getLong(String key, Long def) {
        return toLongExact(get(key, Number.class, def));
    }

    public Long getLong(String key) {
        return getLong(key, 0L);
    }

    public <T> T get(String key, Class<T> type, T def) {
        Object object = rawInput.get(key);
        if (object == null) return def;

        if (!type.isInstance(object)) {
            throw new LoquiDecoderException("Error while decoding content, invalid data type for key: " + key
                    + " found:" + object.getClass().getSimpleName());
        }

        return type.cast(object);
    }

    public <T> T get(String key, Class<T> type) {
        return get(key, type, null);
    }

    private Long toLongExact(Number number) {
        return new BigDecimal(number.toString()).longValueExact();
    }
}
