package it.raniero.fulcrum.api;

import it.raniero.fulcrum.api.command.manager.ICommandRegister;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.io.File;
import java.util.logging.Logger;

/**
 * Describes the host plugin that integrates a platform implementation with Fulcrum.
 */
public interface FulcrumPlugin {

    /**
     * Gets the platform server adapter owned by the plugin.
     *
     * @return platform server adapter
     */
    FulcrumServer getFulcrumServer();

    /**
     * Gets the command register used by the platform.
     *
     * @return command register
     */
    ICommandRegister getCommandRegister();

    /**
     * Gets the plugin data folder.
     *
     * @return plugin data folder
     */
    File getDataFolder();

    /**
     * Gets the plugin logger.
     *
     * @return plugin logger
     */
    Logger getLogger();

    /**
     * Gets the active Fulcrum API instance.
     *
     * @return Fulcrum API instance
     */
    FulcrumAPI getFulcrum();
}
