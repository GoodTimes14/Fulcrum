package it.raniero.fulcrum.velocity.server.sender;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.context.source.SourceType;
import it.raniero.fulcrum.api.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

@RequiredArgsConstructor
public class FulcrumVelocitySource implements FulcrumSource {

    private final CommandSource source;

    @Override
    public Object getSourceObject() {
        return source;
    }

    @Override
    public void sendMessage(String text) {
        String converted = MessageUtils.translateColors(text);
        Component component = LegacyComponentSerializer.legacySection().deserialize(converted);
        source.sendMessage(component);
    }

    @Override
    public void sendMessage(Component component) {
        source.sendMessage(component);
    }

    @Override
    public UUID getUniqueId() {
        return source instanceof Player player ? player.getUniqueId() : null;
    }

    @Override
    public SourceType sourceType() {
        return source instanceof Player ? SourceType.PLAYER : SourceType.CONSOLE;
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return source.hasPermission(permissionNode);
    }
}
