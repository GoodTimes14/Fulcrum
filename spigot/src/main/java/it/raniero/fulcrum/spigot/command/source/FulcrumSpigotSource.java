package it.raniero.fulcrum.spigot.command.source;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.context.source.SourceType;
import it.raniero.fulcrum.api.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class FulcrumSpigotSource implements FulcrumSource {

    private final CommandSender sender;

    @Override
    public Object getSourceObject() {
        return sender;
    }

    @Override
    public void sendMessage(String text) {
        sender.sendMessage(MessageUtils.translateColors(text));
    }

    @Override
    public void sendMessage(Component component) {
        if (sender instanceof Player player) {
            player.spigot().sendMessage(BungeeComponentSerializer.legacy().serialize(component));

        } else {
            sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(component));
        }
    }

    @Override
    public SourceType sourceType() {
        return sender instanceof Player ? SourceType.PLAYER : SourceType.CONSOLE;
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return sender.hasPermission(permissionNode);
    }
}
