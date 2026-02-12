package it.raniero.fulcrum.terminal;

import it.raniero.fulcrum.api.FulcrumAPI;
import it.raniero.fulcrum.api.FulcrumPlugin;
import it.raniero.fulcrum.api.command.manager.ICommandRegister;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.io.File;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FulcrumTerminal implements FulcrumPlugin {

    private final FulcrumServer fulcrumServer;

    private final ICommandRegister commandRegister;

    private final File dataFolder;

    private final Logger logger;

    @Override
    public FulcrumAPI getFulcrum() {
        return null;
    }
}
