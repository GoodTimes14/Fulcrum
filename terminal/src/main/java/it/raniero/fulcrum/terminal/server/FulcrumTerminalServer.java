package it.raniero.fulcrum.terminal.server;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.Getter;

@Getter
public class FulcrumTerminalServer implements FulcrumServer {

    private Predicate<UUID> playerVisibilityPredicate = uuid -> true;

    @Override
    public Class<?> getSenderClass() {
        return FulcrumSource.class;
    }

    @Override
    public Class<?> getPlayerClass() {
        return Void.class;
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
        return playerVisibilityPredicate;
    }

    @Override
    public void setPlayerVisibilityPredicate(Predicate<UUID> predicate) {
        playerVisibilityPredicate = predicate;
    }
}
