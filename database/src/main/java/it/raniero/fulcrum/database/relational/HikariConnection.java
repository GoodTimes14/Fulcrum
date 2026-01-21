package it.raniero.fulcrum.database.relational;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.raniero.fulcrum.database.properties.DatabaseProperties;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class HikariConnection implements RelationalConnection {

    private HikariDataSource dataSource;
    private Logger logger;

    private ExecutorService connectionThreadPool;

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

        logger.log(Level.FINE, "[" + properties.getName() + "]" + "Connection to SQL Database established successfully");
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
    public Future<ResultSet> asyncQuery(Function<Connection, ResultSet> function) {

        Callable<ResultSet> resultTask = () -> {

            try (Connection connection = dataSource.getConnection()) {

                return function.apply(connection);

            } catch (SQLException ex) {

                logger.log(Level.SEVERE,"Error while executing an async query", ex);
                return null;
            }
        };

        return connectionThreadPool.submit(resultTask);
    }

    @Override
    public Future<Integer> asyncUpdate(Function<Connection, Integer> function) {

        Callable<Integer> resultTask = () -> {

            try (Connection connection = dataSource.getConnection()) {

                return function.apply(connection);

            } catch (SQLException ex) {

                logger.log(Level.SEVERE, "Error while executing an async update", ex);
                return null;
            }
        };

        return connectionThreadPool.submit(resultTask);
    }


    @Override
    public Future<Void> asyncInteraction(Consumer<RelationalInteraction> interactionConsumer) {
        Callable<Void> interactionTask = () -> {
            try (Connection connection = dataSource.getConnection()) {

                interactionConsumer.accept(new SQLInteraction(connection, logger));
            } catch (SQLException ex) {

                logger.log(Level.SEVERE, "Error while executing an async interaction", ex);
            }

            return null;
        };

        return connectionThreadPool.submit(interactionTask);
    }

    @Override
    public void close() {
         connectionThreadPool.shutdown();
        try {
            connectionThreadPool.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warning("Thread pool was interrupted before its natural termination, some of the transactions may not have been executed correctly");
        }

        dataSource.close();
    }
}
