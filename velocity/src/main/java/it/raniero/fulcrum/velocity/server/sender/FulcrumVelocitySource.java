package it.raniero.fulcrum.velocity.server.sender;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.context.source.SourceType;
import it.raniero.fulcrum.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@RequiredArgsConstructor
public class FulcrumVelocitySource implements FulcrumSource {

    private final CommandSource source;

    @Override
    public Object getSourceObject() {
        return source;
    }

    @Override
    public void sendMessage(String text) {
        String converted = MessageUtils.tranlateColors(text);
        Component component = LegacyComponentSerializer.legacySection().deserialize(converted);
        source.sendMessage(component);
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
