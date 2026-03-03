package it.raniero.fulcrum.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.FulcrumPlugin;
import it.raniero.fulcrum.api.server.FulcrumServer;
import it.raniero.fulcrum.velocity.command.impl.MainVelocityCommand;
import it.raniero.fulcrum.velocity.command.register.VelocityCommandRegister;
import it.raniero.fulcrum.velocity.server.FulcrumServerVelocity;
import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Plugin(id = "fulcrum", name = "Fulcrum", version = "1.0.0")
@Getter
public class FulcrumVelocity implements FulcrumPlugin {

    private final ProxyServer server;

    private final Logger logger;

    private final Path dataDirectory;

    private final Fulcrum fulcrum;

    private final FulcrumServer fulcrumServer;

    private final VelocityCommandRegister commandRegister;

    @Inject
    public FulcrumVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.server = server;
        this.dataDirectory = dataDirectory;

        File file = dataDirectory.toFile();
        if (file.isDirectory() && !file.exists()) {
            boolean created = file.mkdirs();
            if (created) {
                logger.log(Level.INFO, "Config path created!");
            }
        }

        this.fulcrum = new Fulcrum();
        this.fulcrumServer = new FulcrumServerVelocity(server);
        this.commandRegister = new VelocityCommandRegister(fulcrum, fulcrumServer, server, logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        fulcrum.start(this);
        commandRegister.registerCommand(new MainVelocityCommand(fulcrum));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        fulcrum.stop();
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }
}
