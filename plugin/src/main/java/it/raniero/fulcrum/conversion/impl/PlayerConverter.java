package it.raniero.fulcrum.conversion.impl;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.conversion.IConverter;
import it.raniero.fulcrum.api.server.FulcrumServer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerConverter implements IConverter<FulcrumSource> {

    private final FulcrumServer server;

    @Override
    public Class<FulcrumSource> type() {
        return FulcrumSource.class;
    }

    @Override
    public FulcrumSource convert(String name) {

        FulcrumSource source = server.getOnlinePlayer(name);
        if (source == null || source.getSourceObject() == null) return null;

        return source;
    }

    @Override
    public boolean canConvert(String string) {
        return true;
    }
}
