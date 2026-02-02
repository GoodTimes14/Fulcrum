package it.raniero.fulcrum.database.relational;

import it.raniero.fulcrum.database.properties.DatabaseProperties;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

public interface RelationalConnection {

    void connect(DatabaseProperties properties, Logger logger);

    Connection getConnection();

    RelationalInteraction createInteraction(boolean autoCommit);

    CompletableFuture<ResultSet> asyncQuery(Function<Connection, ResultSet> consumer);

    CompletableFuture<Integer> asyncUpdate(Function<Connection, Integer> consumer);

    CompletableFuture<Void> asyncInteraction(Consumer<RelationalInteraction> consumer);

    CompletableFuture<Void> runAsyncContext(Runnable task);

    <T> CompletableFuture<T> runAsyncStatement(Supplier<T> supplier);

    long getLastActionTime();

    void close();
}
