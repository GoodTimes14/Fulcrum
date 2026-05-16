package it.raniero.fulcrum.api.database.relational;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a closeable SQL interaction with transaction support.
 */
public interface RelationalInteraction extends AutoCloseable {

    /**
     * Executes a SQL query with optional parameters.
     *
     * @param sql query statement
     * @param parameters statement parameters
     * @return query result set
     * @throws SQLException when the query fails
     */
    ResultSet query(String sql, Object... parameters) throws SQLException;

    /**
     * Executes a SQL update with optional parameters.
     *
     * @param sql update statement
     * @param parameters statement parameters
     * @return affected row count
     * @throws SQLException when the update fails
     */
    int update(String sql, Object... parameters) throws SQLException;

    /**
     * Commits the current transaction.
     *
     * @throws SQLException when the commit fails
     */
    void commit() throws SQLException;

    /**
     * Attempts to commit the current transaction without throwing an exception.
     *
     * @return {@code true} when the commit succeeded
     */
    boolean safeCommit();

    /**
     * Rolls back the current transaction.
     *
     * @throws SQLException when the rollback fails
     */
    void rollback() throws SQLException;

    /**
     * Checks whether auto-commit is enabled.
     *
     * @return {@code true} when auto-commit is enabled
     * @throws SQLException when the connection state cannot be read
     */
    boolean isAutoCommit() throws SQLException;

    /**
     * Sets the auto-commit mode for this interaction.
     *
     * @param autoCommit whether auto-commit should be enabled
     * @throws SQLException when the auto-commit mode cannot be changed
     */
    void autoCommit(boolean autoCommit) throws SQLException;
}
