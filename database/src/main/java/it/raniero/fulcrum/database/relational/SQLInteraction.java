package it.raniero.fulcrum.database.relational;

import it.raniero.fulcrum.api.database.query.utils.StatementUtils;
import it.raniero.fulcrum.api.database.relational.RelationalInteraction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SQLInteraction implements RelationalInteraction {

    private final Connection connection;

    private final Logger logger;

    private final Set<PreparedStatement> aliveStatements = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public ResultSet query(String sql, Object... parameters) throws SQLException {
        PreparedStatement statement = StatementUtils.createStatement(connection, sql, parameters);
        aliveStatements.add(statement);

        return statement.executeQuery();
    }

    @Override
    public int update(String sql, Object... parameters) throws SQLException {
        try (PreparedStatement statement = StatementUtils.createStatement(connection, sql, parameters)) {
            return statement.executeUpdate();
        }
    }

    @Override
    public void commit() throws SQLException {
        connection.commit();
    }

    @Override
    public boolean safeCommit() {

        try {

            connection.commit();

            return true;
        } catch (SQLException e) {

            logger.log(Level.SEVERE, "Safe commit returned error: ", e);

            logger.log(Level.INFO, "trying rollback...");

            try {
                connection.rollback();

            } catch (Exception exception) {

                logger.log(Level.SEVERE, "Rollback failed", exception);
            }

            return false;
        }
    }

    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }

    @Override
    public boolean isAutoCommit() throws SQLException {
        return connection.getAutoCommit();
    }

    @Override
    public void autoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }

    @Override
    public void close() throws SQLException {
        for (PreparedStatement aliveStatement : aliveStatements) {
            aliveStatement.close();
        }
        aliveStatements.clear();
        connection.close();
    }
}
