package it.raniero.fulcrum.conversion.impl;

import it.raniero.fulcrum.api.conversion.IConverter;

public class LongConverter implements IConverter<Long> {
    @Override
    public Class<Long> type() {
        return Long.class;
    }

    @Override
    public Long convert(String string) {
        return Long.parseLong(string, 10);
    }

    @Override
    public boolean canConvert(String string) {
        try {
            long l = Long.parseLong(string, 10);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
