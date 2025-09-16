package it.raniero.fulcrum.database.relational;

import it.raniero.fulcrum.database.properties.DatabaseProperties;
import java.sql.Connection;
import java.util.function.Consumer;
import java.util.logging.Logger;

public interface RelationalConnection {

    void connect(DatabaseProperties properties, Logger logger);

    Connection getConnection();

    RelationalInteraction createInteraction(boolean autoCommit);

    void asyncStatement(Consumer<Connection> consumer);

    void asyncInteraction(Consumer<RelationalInteraction> consumer);

    void asyncQueue();

    void close();
}
