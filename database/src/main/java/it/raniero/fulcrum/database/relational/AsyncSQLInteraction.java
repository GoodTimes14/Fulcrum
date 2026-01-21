package it.raniero.fulcrum.database.relational;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AsyncSQLInteraction implements RelationalInteraction {


    @Override
    public ResultSet query(String sql, Object... parameters) throws SQLException {
        return null;
    }

    @Override
    public int update(String sql, Object... parameters) throws SQLException {
        return 0;
    }

    @Override
    public void commit() throws SQLException {
        throw new UnsupportedOperationException("Can't use transactions in async contexts yet");
    }

    @Override
    public boolean safeCommit() {
        throw new UnsupportedOperationException("Can't use transactions in async contexts yet");
    }

    @Override
    public void rollback() throws SQLException {
        throw new UnsupportedOperationException("Can't use transactions in async contexts yet");
    }

    @Override
    public boolean isAutoCommit() throws SQLException {
        return true;
    }

    @Override
    public void autoCommit(boolean autoCommit) throws SQLException {
        throw new UnsupportedOperationException("Can't use transactions in async contexts yet");
    }

    @Override
    public void close() throws Exception {

    }
}
