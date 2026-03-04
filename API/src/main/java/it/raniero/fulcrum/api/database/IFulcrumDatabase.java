package it.raniero.fulcrum.api.database;

import it.raniero.fulcrum.api.database.properties.ConnectionType;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.database.redis.IRedisConnection;
import it.raniero.fulcrum.api.database.relational.RelationalConnection;
import java.util.Map;
import java.util.Optional;

public interface IFulcrumDatabase {

    void registerConnection(DatabaseProperties properties);

    boolean unregisterConnection(ConnectionType connectionType, String name);

    Optional<RelationalConnection> getDatabaseConnection(String name);

    Optional<IRedisConnection> getRedisConnection(String name);

    Map<String, RelationalConnection> getAllRelationalConnections();

    Map<String, IRedisConnection> getAllRedisConnections();

    void closeConnections();
}
