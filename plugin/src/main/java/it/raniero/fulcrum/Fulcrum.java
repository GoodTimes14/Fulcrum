package it.raniero.fulcrum;

import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.manager.CommandManager;
import it.raniero.fulcrum.conversion.ConversionManager;
import it.raniero.fulcrum.conversion.IConversionManager;
import it.raniero.fulcrum.utils.DatabaseProperties;
import it.raniero.fulcrum.utils.RedisProperties;
import lombok.Getter;

@Getter
public class Fulcrum implements FulcrumAPI {

    private final IConversionManager conversionManager = new ConversionManager(this);
    private final CommandManager commandManager = new CommandManager();
    private FulcrumPlugin plugin;

    @Override
    public void start(FulcrumPlugin plugin) {
        this.plugin = plugin;
        conversionManager.init();
    }

    @Override
    public void createDatabase(DatabaseProperties databaseProperties) {}

    @Override
    public void createRedis(RedisProperties redisProperties) {}

    public void help(ICommandContext context) {}

    @Override
    public void stop() {}
}
