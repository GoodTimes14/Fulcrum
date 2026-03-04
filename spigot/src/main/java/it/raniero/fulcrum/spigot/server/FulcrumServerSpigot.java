package it.raniero.fulcrum.spigot.server;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.spigot.command.source.FulcrumSpigotSource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Getter
public class FulcrumServerSpigot implements FulcrumServer {

    private Predicate<UUID> playerVisibilityPredicate = (uuid) -> true;

    @Override
    public Class<?> getSenderClass() {
        return CommandSender.class;
    }

    @Override
    public Class<?> getPlayerClass() {
        return Player.class;
    }

    @Override
    public List<String> getOnlinePlayerNames(String pattern) {

        List<String> names = new ArrayList<>();

        pattern = pattern.toLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(pattern)) {
                names.add(player.getName());
            }
        }

        return names;
    }

    @Override
    public FulcrumSource getOnlinePlayer(String name) {
        return new FulcrumSpigotSource(Bukkit.getPlayer(name));
    }

    @Override
    public FulcrumSource getOnlinePlayer(UUID uuid) {
        return new FulcrumSpigotSource(Bukkit.getPlayer(uuid));
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
