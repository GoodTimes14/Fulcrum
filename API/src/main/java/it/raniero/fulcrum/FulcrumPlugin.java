package it.raniero.fulcrum;

import it.raniero.fulcrum.command.manager.ICommandRegister;
import it.raniero.fulcrum.server.FulcrumServer;
import java.io.File;
import java.util.logging.Logger;

public interface FulcrumPlugin {

    FulcrumServer getFulcrumServer();

    ICommandRegister getCommandRegister();

    File getDataFolder();

    Logger getLogger();
}
