package it.raniero.fulcrum;

import it.raniero.fulcrum.api.FulcrumAPI;
import it.raniero.fulcrum.api.FulcrumPlugin;
import it.raniero.fulcrum.api.conversion.IConversionManager;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.command.manager.CommandManager;
import it.raniero.fulcrum.config.FulcrumConfig;
import it.raniero.fulcrum.config.holder.FulcrumDatabaseHolder;
import it.raniero.fulcrum.config.holder.FulcrumMessagesHolder;
import it.raniero.fulcrum.conversion.ConversionManager;
import it.raniero.fulcrum.database.FulcrumDatabase;
import java.util.List;
import lombok.Getter;

@Getter
public class Fulcrum implements FulcrumAPI {

    private final IConversionManager conversionManager = new ConversionManager(this);

    private final CommandManager commandManager = new CommandManager(this);

    private FulcrumDatabase database;

    private FulcrumPlugin plugin;

    private FulcrumConfig mainConfig;

    @Override
    public void start(FulcrumPlugin plugin) {
        this.plugin = plugin;
        database = new FulcrumDatabase(plugin.getLogger());
        mainConfig = new FulcrumConfig();

        createConfig();
        registerDatabases();

        conversionManager.init();
    }

    private void createConfig() {
        mainConfig.initConfig(plugin.getDataFolder(), "messages.yml", FulcrumMessagesHolder.class);
        mainConfig.initConfig(plugin.getDataFolder(), "databases.yml", FulcrumDatabaseHolder.class);
    }

    private void registerDatabases() {
        List<DatabaseProperties> databases =
                mainConfig.get(FulcrumDatabaseHolder.class, FulcrumDatabaseHolder.DATABASES);
        for (DatabaseProperties databaseProperties : databases) {
            if (databaseProperties.isEnabled()) {
                database.registerConnection(databaseProperties);
            }
        }
    }

    @Override
    public void stop() {
        plugin.getCommandRegister().unregisterCommands();
        database.closeConnections();
    }
}
