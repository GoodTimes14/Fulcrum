package it.raniero.fulcrum.conversion.impl;

import it.raniero.fulcrum.api.conversion.IConverter;

public class StringConverter implements IConverter<String> {

    @Override
    public Class<String> type() {
        return String.class;
    }

    @Override
    public String convert(String string) {
        return string;
    }

    @Override
    public boolean canConvert(String string) {
        return true;
    }
}
