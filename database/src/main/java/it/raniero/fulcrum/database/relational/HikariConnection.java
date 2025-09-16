package it.raniero.fulcrum.database.relational;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.raniero.fulcrum.database.properties.DatabaseProperties;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HikariConnection implements RelationalConnection {

    private HikariDataSource dataSource;
    private Logger logger;
    private BlockingQueue<Consumer<Connection>> taskQueue = new LinkedBlockingQueue<>();

    private Thread asyncThread;

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

        asyncThread = new Thread(this::asyncQueue);
        asyncThread.start();

        logger.log(Level.FINE, "Connection to SQL Database established successfully");
    }

    @Override
    public Connection getConnection() {
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

            return interaction;

        } catch (SQLException e) {

            logger.log(Level.SEVERE, "Error while creating interaction: ", e);
            return null;
        }
    }

    @Override
    public void asyncQueue() {
        while (!Thread.currentThread().isInterrupted()) {

            try {

                Consumer<Connection> consumer = taskQueue.take();
                executeAsyncStatement(consumer);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }
        }
    }

    private void executeAsyncStatement(Consumer<Connection> consumer) {
        try (Connection connection = dataSource.getConnection()) {

            consumer.accept(connection);

        } catch (SQLException ex) {

            logger.log(Level.SEVERE, "Error while executing an async statement", ex);
        }
    }

    @Override
    public void asyncStatement(Consumer<Connection> consumer) {
        try {
            taskQueue.put(consumer);
        } catch (InterruptedException e) {
            // Ignoriamo
        }
    }

    public void asyncInteraction(Consumer<RelationalInteraction> interactionConsumer) {
        try {

            Consumer<Connection> connectionConsumer = (connection) -> {
                interactionConsumer.accept(new SQLInteraction(connection, logger));
            };

            taskQueue.put(connectionConsumer);

        } catch (InterruptedException e) {
            // Ignoriamo
        }
    }

    @Override
    public void close() {
        asyncThread.interrupt();
        while (!taskQueue.isEmpty()) {
            executeAsyncStatement(taskQueue.poll());
        }
        dataSource.close();
    }
}
