package it.raniero.fulcrum;

import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.scheme.CommandScheme;
import it.raniero.fulcrum.command.scheme.arguments.CommandArgument;
import it.raniero.fulcrum.utils.DatabaseProperties;
import it.raniero.fulcrum.utils.RedisProperties;
import it.raniero.fulcrum.utils.StartupProperties;

public class Fulcrum implements FulcrumAPI {


    @Override
    public void start(StartupProperties startupProperties) {
        CommandScheme.builder().argument(CommandArgument.builder().name().build())
    }

    @Override
    public void createDatabase(DatabaseProperties databaseProperties) {

    }

    @Override
    public void createRedis(RedisProperties redisProperties) {

    }

    public void help(ICommandContext context) {

    }

    @Override
    public void stop() {

    }
}
