package it.raniero.fulcrum.api.database.relational;

import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Represents a relational database connection and asynchronous execution facility.
 */
public interface RelationalConnection {

    /**
     * Opens the connection using the supplied properties.
     *
     * @param properties database connection properties
     * @param logger logger for connection diagnostics
     */
    void connect(DatabaseProperties properties, Logger logger);

    /**
     * Gets the properties used by this connection.
     *
     * @return database connection properties
     */
    DatabaseProperties properties();

    /**
     * Gets the underlying JDBC connection.
     *
     * @return JDBC connection
     */
    Connection getConnection();

    /**
     * Creates a SQL interaction with the requested auto-commit mode.
     *
     * @param autoCommit whether auto-commit should be enabled
     * @return relational interaction
     */
    RelationalInteraction createInteraction(boolean autoCommit);

    /**
     * Executes a query asynchronously against the underlying connection.
     *
     * @param consumer query function
     * @return future result set
     */
    CompletableFuture<ResultSet> asyncQuery(Function<Connection, ResultSet> consumer);

    /**
     * Executes an update asynchronously against the underlying connection.
     *
     * @param consumer update function
     * @return future affected row count
     */
    CompletableFuture<Integer> asyncUpdate(Function<Connection, Integer> consumer);

    /**
     * Executes an interaction asynchronously.
     *
     * @param consumer interaction consumer
     * @return completion future
     */
    CompletableFuture<Void> asyncInteraction(Consumer<RelationalInteraction> consumer);

    /**
     * Executes a supplier asynchronously.
     *
     * @param supplier supplier to execute
     * @param <T> result type
     * @return future supplier result
     */
    <T> CompletableFuture<T> async(Supplier<T> supplier);

    /**
     * Executes an action asynchronously.
     *
     * @param action action to execute
     * @return completion future
     */
    CompletableFuture<Void> async(Runnable action);

    /**
     * Gets the last time this connection performed an action.
     *
     * @return last action timestamp
     */
    long getLastActionTime();

    /**
     * Closes the connection and releases resources.
     */
    void close();
}
