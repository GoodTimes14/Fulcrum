package it.raniero.fulcrum.conversion.impl;

import it.raniero.fulcrum.conversion.IConverter;

public class DoubleConverter implements IConverter<Double> {


    @Override
    public Class<Double> type() {
        return Double.class;
    }

    @Override
    public Double convert(String string) {
        return Double.parseDouble(string);
    }

    @Override
    public boolean canConvert(String string) {
        try {
            double x = Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

