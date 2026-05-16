package it.raniero.fulcrum.terminal.server.sender;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.context.source.SourceType;
import it.raniero.fulcrum.api.utils.MessageUtils;
import java.io.PrintStream;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Getter
@RequiredArgsConstructor
public class TerminalSource implements FulcrumSource {

    private final PrintStream outputStream;

    @Override
    public Object getSourceObject() {
        return outputStream;
    }

    @Override
    public void sendMessage(String mess) {
        Component component = LegacyComponentSerializer.legacySection().deserialize(MessageUtils.translateColors(mess));
        sendMessage(component);
    }

    @Override
    public void sendMessage(Component component) {
        String serialized = ANSIComponentSerializer.ansi().serialize(component);
        outputStream.println(serialized);
    }

    @Override
    public UUID getUniqueId() {
        return null;
    }

    @Override
    public String getName() {
        return "Terminal";
    }

    @Override
    public SourceType sourceType() {
        return SourceType.CONSOLE;
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return true;
    }
}
