package it.raniero.fulcrum;

import it.raniero.fulcrum.command.manager.ICommandRegister;
import it.raniero.fulcrum.server.FulcrumServer;

public interface FulcrumPlugin {

    FulcrumServer getFulcrumServer();

    ICommandRegister getCommmandRegister();
}
