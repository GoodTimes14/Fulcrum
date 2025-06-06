package it.raniero.fulcrum.spigot.server;

import com.google.common.base.Predicates;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.server.FulcrumServer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

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

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().startsWith(pattern)) {
                names.add(player.getName());
            }
        }

        return names;
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
