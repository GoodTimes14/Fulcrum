package it.raniero.fulcrum.terminal.server;

import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.server.FulcrumServer;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class FulcrumTerminalServer implements FulcrumServer {

    @Override
    public Class<?> getSenderClass() {
        return null;
    }

    @Override
    public Class<?> getPlayerClass() {
        return null;
    }

    @Override
    public List<String> getOnlinePlayerNames(String pattern) {
        return List.of();
    }

    @Override
    public FulcrumSource getOnlinePlayer(String name) {
        return null;
    }

    @Override
    public FulcrumSource getOnlinePlayer(UUID uuid) {
        return null;
    }

    @Override
    public Predicate<UUID> getPlayerVisibilityPredicate() {
        return null;
    }

    @Override
    public void setPlayerVisibilityPredicate(Predicate<UUID> predicate) {}
}
