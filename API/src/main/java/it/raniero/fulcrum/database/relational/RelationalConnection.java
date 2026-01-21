package it.raniero.fulcrum.database.relational;

import it.raniero.fulcrum.database.properties.DatabaseProperties;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public interface RelationalConnection {

    void connect(DatabaseProperties properties, Logger logger);

    Connection getConnection();

    RelationalInteraction createInteraction(boolean autoCommit);

    Future<ResultSet> asyncQuery(Function<Connection, ResultSet> consumer);

    Future<Integer> asyncUpdate(Function<Connection, Integer> consumer);

    Future<Void> asyncInteraction(Consumer<RelationalInteraction> consumer);

    void close();
}
