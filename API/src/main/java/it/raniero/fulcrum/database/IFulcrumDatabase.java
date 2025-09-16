package it.raniero.fulcrum.database;

import it.raniero.fulcrum.database.properties.ConnectionType;
import it.raniero.fulcrum.database.properties.DatabaseProperties;
import it.raniero.fulcrum.database.redis.IRedisConnection;
import it.raniero.fulcrum.database.relational.RelationalConnection;
import java.util.Optional;

public interface IFulcrumDatabase {

    void registerConnection(DatabaseProperties properties);

    boolean unregisterConnection(ConnectionType connectionType, String name);

    Optional<RelationalConnection> getDatabaseConnection(String name);

    Optional<IRedisConnection> getRedisConnection(String name);

    void closeConnections();
}
