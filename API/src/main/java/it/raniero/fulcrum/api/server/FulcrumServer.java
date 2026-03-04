package it.raniero.fulcrum.api.server;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface FulcrumServer {

    Class<?> getSenderClass();

    Class<?> getPlayerClass();

    List<String> getOnlinePlayerNames(String pattern);

    FulcrumSource getOnlinePlayer(String name);

    FulcrumSource getOnlinePlayer(UUID uuid);

    Predicate<UUID> getPlayerVisibilityPredicate();

    void setPlayerVisibilityPredicate(Predicate<UUID> predicate);
}
