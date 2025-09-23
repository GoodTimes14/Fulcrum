package it.raniero.fulcrum.conversion.impl;

import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.conversion.IConverter;
import it.raniero.fulcrum.server.FulcrumServer;
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

        return server.getOnlinePlayer(name);
    }

    @Override
    public boolean canConvert(String string) {
        return true;
    }
}
