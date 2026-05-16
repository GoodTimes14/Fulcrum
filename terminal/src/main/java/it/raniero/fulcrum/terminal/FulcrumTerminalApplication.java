package it.raniero.fulcrum.terminal;

import java.io.IOException;

public final class FulcrumTerminalApplication {

    private FulcrumTerminalApplication() {
        throw new IllegalStateException("Utility class");
    }

    public static void main(String[] args) throws IOException {
        FulcrumTerminal terminal = new FulcrumTerminal();
        terminal.start();
    }
}
