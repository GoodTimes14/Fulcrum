package it.raniero.fulcrum.bungeecord.server.source;

import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.context.source.SourceType;
import lombok.RequiredArgsConstructor;
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
    public SourceType sourceType() {
        return sender instanceof ProxiedPlayer ? SourceType.PLAYER : SourceType.CONSOLE;
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return sender.hasPermission(permissionNode);
    }
}
