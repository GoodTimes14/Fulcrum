package it.raniero.fulcrum.bungeecord.server;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.bungeecord.server.source.FulcrumBungeeCordSource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class FulcrumBungeeCordServer implements FulcrumServer {

    private Predicate<UUID> playerVisibilityPredicate = (uuid) -> true;

    @Override
    public Class<?> getSenderClass() {
        return CommandSender.class;
    }

    @Override
    public Class<?> getPlayerClass() {
        return ProxiedPlayer.class;
    }

    @Override
    public List<String> getOnlinePlayerNames(String pattern) {
        List<String> names = new ArrayList<>();

        pattern = pattern.toLowerCase();
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.getName().toLowerCase().startsWith(pattern)) {
                names.add(player.getName());
            }
        }

        return names;
    }

    @Override
    public FulcrumSource getOnlinePlayer(String name) {
        return new FulcrumBungeeCordSource(ProxyServer.getInstance().getPlayer(name));
    }

    @Override
    public FulcrumSource getOnlinePlayer(UUID uuid) {
        return new FulcrumBungeeCordSource(ProxyServer.getInstance().getPlayer(uuid));
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
