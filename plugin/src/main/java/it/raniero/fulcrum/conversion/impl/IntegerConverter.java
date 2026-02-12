package it.raniero.fulcrum.conversion.impl;

import it.raniero.fulcrum.api.conversion.IConverter;

public class IntegerConverter implements IConverter<Integer> {

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public Integer convert(String string) {
        return Integer.parseInt(string);
    }

    @Override
    public boolean canConvert(String string) {
        try {
            int x = Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
