package it.raniero.fulcrum;

import it.raniero.fulcrum.database.properties.DatabaseProperties;
import it.raniero.fulcrum.utils.RedisProperties;

public interface FulcrumAPI {

    void start(FulcrumPlugin plugin);

    void createDatabase(DatabaseProperties databaseProperties);

    void createRedis(RedisProperties redisProperties);

    void stop();
}
