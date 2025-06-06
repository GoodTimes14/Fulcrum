package it.raniero.fulcrum.spigot.command.source;

import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.context.source.SourceType;
import lombok.RequiredArgsConstructor;
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
        sender.sendMessage(text);
    }

    @Override
    public SourceType sourceType() {
        return sender instanceof Player ? SourceType.PLAYER : SourceType.CONSOLE;
    }
}
