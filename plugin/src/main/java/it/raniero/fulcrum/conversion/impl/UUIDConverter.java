package it.raniero.fulcrum.conversion.impl;

import it.raniero.fulcrum.conversion.IConverter;

import java.util.UUID;

public class UUIDConverter  implements IConverter<UUID> {
    @Override
    public Class<UUID> type() {
        return UUID.class;
    }

    @Override
    public UUID convert(String string) {
        return UUID.fromString(string);
    }

    @Override
    public boolean canConvert(String string) {
        try {
            UUID uuid = UUID.fromString(string);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
