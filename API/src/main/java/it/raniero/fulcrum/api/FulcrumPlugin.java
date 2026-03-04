package it.raniero.fulcrum.api;

import it.raniero.fulcrum.api.command.manager.ICommandRegister;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.io.File;
import java.util.logging.Logger;

public interface FulcrumPlugin {

    FulcrumServer getFulcrumServer();

    ICommandRegister getCommandRegister();

    File getDataFolder();

    Logger getLogger();

    FulcrumAPI getFulcrum();
}
