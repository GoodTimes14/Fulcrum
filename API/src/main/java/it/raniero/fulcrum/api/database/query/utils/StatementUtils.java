package it.raniero.fulcrum.api.database.query.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementUtils {

    private StatementUtils() {
        throw new IllegalArgumentException("Utility Class");
    }

    public static PreparedStatement createStatement(Connection connection, String sql, Object... parameters)
            throws SQLException {

        PreparedStatement statement = connection.prepareStatement(sql);

        for (int i = 0; i < parameters.length; i++) {
            Object param = parameters[i];
            statement.setObject(i + 1, param);
        }
        return statement;
    }
}
