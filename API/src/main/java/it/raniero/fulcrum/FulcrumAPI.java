package it.raniero.fulcrum;

import it.raniero.fulcrum.utils.DatabaseProperties;
import it.raniero.fulcrum.utils.RedisProperties;
import it.raniero.fulcrum.utils.StartupProperties;

public interface FulcrumAPI {

    void start(StartupProperties startupProperties);

    void createDatabase(DatabaseProperties databaseProperties);

    void createRedis(RedisProperties redisProperties);

    void stop();


}
