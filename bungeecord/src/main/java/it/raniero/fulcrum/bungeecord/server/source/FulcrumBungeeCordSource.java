package it.raniero.fulcrum.bungeecord.server.source;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.context.source.SourceType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@RequiredArgsConstructor
public class FulcrumBungeeCordSource implements FulcrumSource {

    private final CommandSender sender;

    @Override
    public Object getSourceObject() {
        return sender;
    }

    @Override
    public void sendMessage(String text) {
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', text)));
    }

    @Override
    public void sendMessage(Component component) {
        sender.sendMessage(BungeeComponentSerializer.legacy().serialize(component));
    }

    @Override
    public UUID getUniqueId() {
        return sender instanceof ProxiedPlayer player ? player.getUniqueId() : null;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public SourceType sourceType() {
        return sender instanceof ProxiedPlayer ? SourceType.PLAYER : SourceType.CONSOLE;
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return sender.hasPermission(permissionNode);
    }
}
