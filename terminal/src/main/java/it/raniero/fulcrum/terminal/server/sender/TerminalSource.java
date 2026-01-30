package it.raniero.fulcrum.terminal.server.sender;

import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.command.context.source.SourceType;
import java.io.PrintStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    public SourceType sourceType() {
        return SourceType.PLAYER;
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return true;
    }
}
