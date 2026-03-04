package it.raniero.fulcrum.database;

import it.raniero.fulcrum.api.database.IFulcrumDatabase;
import it.raniero.fulcrum.api.database.properties.ConnectionType;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.database.redis.IRedisConnection;
import it.raniero.fulcrum.api.database.relational.RelationalConnection;
import it.raniero.fulcrum.database.redis.LettuceConnection;
import it.raniero.fulcrum.database.relational.HikariConnection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FulcrumDatabase implements IFulcrumDatabase {

    private final Logger logger;

    @Getter
    private final Map<String, RelationalConnection> databaseConnections = new ConcurrentHashMap<>();

    @Getter
    private final Map<String, IRedisConnection> redisConnections = new ConcurrentHashMap<>();

    @Override
    public void registerConnection(DatabaseProperties properties) {
        if (properties.getConnectionType() == ConnectionType.SQL) {

            HikariConnection hikariConnection = new HikariConnection();

            try {
                hikariConnection.connect(properties, logger);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error while trying to connect to database: " + properties.getName(), e);
                return;
            }

            databaseConnections.put(properties.getName(), hikariConnection);

        } else {

            try {
                LettuceConnection lettuceConnection = new LettuceConnection(logger, properties);

                redisConnections.put(properties.getName(), lettuceConnection);

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error while trying to connect to redis: " + properties.getName(), e);
            }
        }
    }

    @Override
    public boolean unregisterConnection(ConnectionType connectionType, String name) {
        if (connectionType == ConnectionType.SQL) {

            RelationalConnection connection = databaseConnections.remove(name);
            if (connection != null) {
                connection.close();
                return true;
            }

            return false;
        } else {

            IRedisConnection redisConnection = redisConnections.remove(name);
            if (redisConnection != null) {
                redisConnection.close();
                return true;
            }

            return false;
        }
    }

    @Override
    public Optional<RelationalConnection> getDatabaseConnection(String name) {
        return Optional.ofNullable(databaseConnections.get(name));
    }

    @Override
    public Optional<IRedisConnection> getRedisConnection(String name) {
        return Optional.ofNullable(redisConnections.get(name));
    }

    @Override
    public Map<String, RelationalConnection> getAllRelationalConnections() {
        return Map.copyOf(databaseConnections);
    }

    @Override
    public Map<String, IRedisConnection> getAllRedisConnections() {
        return Map.copyOf(redisConnections);
    }

    @Override
    public void closeConnections() {
        for (RelationalConnection connection : databaseConnections.values()) {
            connection.close();
        }

        for (IRedisConnection connection : redisConnections.values()) {
            connection.close();
        }
    }
}
