package it.raniero.fulcrum.api;

import it.raniero.fulcrum.api.database.IFulcrumDatabase;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.utils.RedisProperties;

public interface FulcrumAPI {

    void start(FulcrumPlugin plugin);

    IFulcrumDatabase getDatabase();

    FulcrumPlugin getPlugin();

    void createDatabase(DatabaseProperties databaseProperties);

    void createRedis(RedisProperties redisProperties);

    void stop();
}
