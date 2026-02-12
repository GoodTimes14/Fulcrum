package it.raniero.fulcrum.velocity.server;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.velocity.server.sender.FulcrumVelocitySource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FulcrumServerVelocity implements FulcrumServer {

    private final ProxyServer server;

    private Predicate<UUID> visibilityPredicate = uuid -> true;

    @Override
    public Class<?> getSenderClass() {
        return CommandSource.class;
    }

    @Override
    public Class<?> getPlayerClass() {
        return Player.class;
    }

    @Override
    public List<String> getOnlinePlayerNames(String pattern) {
        return new ArrayList<>(
                server.getAllPlayers().stream().map(Player::getUsername).toList());
    }

    @Override
    public FulcrumSource getOnlinePlayer(String name) {
        return new FulcrumVelocitySource(server.getPlayer(name).orElse(null));
    }

    @Override
    public FulcrumSource getOnlinePlayer(UUID uuid) {
        return new FulcrumVelocitySource(server.getPlayer(uuid).orElse(null));
    }

    @Override
    public Predicate<UUID> getPlayerVisibilityPredicate() {
        return visibilityPredicate;
    }

    @Override
    public void setPlayerVisibilityPredicate(Predicate<UUID> predicate) {
        this.visibilityPredicate = predicate;
    }
}
