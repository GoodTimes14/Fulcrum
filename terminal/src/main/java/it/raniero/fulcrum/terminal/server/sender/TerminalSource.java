package it.raniero.fulcrum.terminal.server.sender;

import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.context.source.SourceType;
import java.io.PrintStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

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
        outputStream.println(mess);
    }

    @Override
    public void sendMessage(Component component) {
        outputStream.println("");
    }

    @Override
    public SourceType sourceType() {
        return SourceType.PLAYER;
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return true;
    }
}
