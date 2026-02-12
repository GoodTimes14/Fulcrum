package it.raniero.fulcrum.api.database.relational;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RelationalInteraction extends AutoCloseable {

    ResultSet query(String sql, Object... parameters) throws SQLException;

    int update(String sql, Object... parameters) throws SQLException;

    void commit() throws SQLException;

    boolean safeCommit();

    void rollback() throws SQLException;

    boolean isAutoCommit() throws SQLException;

    void autoCommit(boolean autoCommit) throws SQLException;
}
