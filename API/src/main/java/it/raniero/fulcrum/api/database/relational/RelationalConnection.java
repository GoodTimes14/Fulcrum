package it.raniero.fulcrum.api.database.relational;

import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

public interface RelationalConnection {

    void connect(DatabaseProperties properties, Logger logger);

    DatabaseProperties properties();

    Connection getConnection();

    RelationalInteraction createInteraction(boolean autoCommit);

    CompletableFuture<ResultSet> asyncQuery(Function<Connection, ResultSet> consumer);

    CompletableFuture<Integer> asyncUpdate(Function<Connection, Integer> consumer);

    CompletableFuture<Void> asyncInteraction(Consumer<RelationalInteraction> consumer);

    <T> CompletableFuture<T> async(Supplier<T> supplier);

    CompletableFuture<Void> async(Runnable action);

    long getLastActionTime();

    void close();
}
