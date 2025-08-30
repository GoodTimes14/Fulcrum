package it.raniero.fulcrum;

import it.raniero.fulcrum.utils.DatabaseProperties;
import it.raniero.fulcrum.utils.RedisProperties;

public interface FulcrumAPI {

    void start(FulcrumPlugin plugin);

    void createDatabase(DatabaseProperties databaseProperties);

    void createRedis(RedisProperties redisProperties);

    void stop();
}
