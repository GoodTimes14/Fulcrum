package it.raniero.fulcrum.database.relational;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.raniero.fulcrum.database.properties.DatabaseProperties;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

public class HikariConnection implements RelationalConnection {

    private HikariDataSource dataSource;
    private Logger logger;

    private ExecutorService connectionThreadPool;

    @Getter
    private long lastActionTime;

    @Override
    public void connect(DatabaseProperties properties, Logger logger) {
        this.logger = logger;
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(
                "jdbc:mysql://" + properties.getHost() + ":" + properties.getPort() + "/" + properties.getDatabase());
        config.setUsername(properties.getUsername());
        if (properties.isAuth()) {
            config.setPassword(properties.getPassword());
        }

        config.setDriverClassName("it.raniero.fulcrum.libs.com.mysql.cj.jdbc.Driver");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.addDataSourceProperty("enabledTLSProtocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
        config.addDataSourceProperty("databaseName", properties.getDatabase());
        config.addDataSourceProperty("autoReconnect", true);
        config.addDataSourceProperty("allowMultiQueries", true);

        dataSource = new HikariDataSource(config);

        connectionThreadPool = Executors.newFixedThreadPool(1);

        logger.log(
                Level.FINE, "[" + properties.getName() + "]" + "Connection to SQL Database established successfully");
    }

    @Override
    public Connection getConnection() {
        lastActionTime = System.currentTimeMillis();
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {

            logger.log(Level.SEVERE, "Error while creating connection: ", e);
            return null;
        }
    }

    @Override
    public RelationalInteraction createInteraction(boolean autoCommit) {
        try {

            SQLInteraction interaction = new SQLInteraction(getConnection(), logger);
            interaction.autoCommit(autoCommit);
            lastActionTime = System.currentTimeMillis();

            return interaction;

        } catch (SQLException e) {

            logger.log(Level.SEVERE, "Error while creating interaction: ", e);
            return null;
        }
    }

    @Override
    public CompletableFuture<ResultSet> asyncQuery(Function<Connection, ResultSet> function) {
        lastActionTime = System.currentTimeMillis();
        Supplier<ResultSet> resultTask = () -> {
            try (Connection connection = dataSource.getConnection()) {

                return function.apply(connection);

            } catch (SQLException ex) {

                logger.log(Level.SEVERE, "Error while executing an async query", ex);
                return null;
            }
        };

        return CompletableFuture.supplyAsync(resultTask, connectionThreadPool);
    }

    @Override
    public CompletableFuture<Integer> asyncUpdate(Function<Connection, Integer> function) {

        lastActionTime = System.currentTimeMillis();
        Supplier<Integer> resultTask = () -> {
            try (Connection connection = dataSource.getConnection()) {

                return function.apply(connection);

            } catch (SQLException ex) {

                logger.log(Level.SEVERE, "Error while executing an async update", ex);
                return null;
            }
        };



        return CompletableFuture.supplyAsync(resultTask, connectionThreadPool);
    }

    @Override
    public CompletableFuture<Void> asyncInteraction(Consumer<RelationalInteraction> interactionConsumer) {
        lastActionTime = System.currentTimeMillis();
        Runnable interactionTask = () -> {
            try (Connection connection = dataSource.getConnection()) {

                interactionConsumer.accept(new SQLInteraction(connection, logger));
            } catch (SQLException ex) {

                logger.log(Level.SEVERE, "Error while executing an async interaction", ex);
            }
        };

        return CompletableFuture.runAsync(interactionTask, connectionThreadPool);
    }


    @Override
    public <T> CompletableFuture<T> runAsyncStatement(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, connectionThreadPool);
    }


    @Override
    public CompletableFuture<Void> runAsyncContext(Runnable task) {
        lastActionTime = System.currentTimeMillis();
        return CompletableFuture.runAsync(task, connectionThreadPool);
    }

    @Override
    public void close() {
        connectionThreadPool.shutdown();
        try {
            boolean terminated = connectionThreadPool.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warning(
                    "Thread pool was interrupted before its natural termination, some of the transactions may not have been executed correctly");
        }

        dataSource.close();
    }
}
