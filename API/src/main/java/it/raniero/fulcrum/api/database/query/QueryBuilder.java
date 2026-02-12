package it.raniero.fulcrum.api.database.query;

import it.raniero.fulcrum.api.database.query.utils.QueryCondition;

public class QueryBuilder {

    private final StringBuilder query = new StringBuilder();

    public String build() {
        return query.toString();
    }

    public QueryBuilder select(String table, String... columns) {

        StringBuilder fields = new StringBuilder();

        for (int i = 0; i < columns.length; i++) {

            String column = columns[i];
            fields.append(column);

            if (i + 1 != columns.length) {
                fields.append(",");
            }
        }

        query.append("SELECT ").append(fields).append(" FROM ").append(table);
        return this;
    }

    public QueryBuilder where(QueryCondition condition) {
        query.append(" WHERE ").append(condition.getCondition());
        return this;
    }

    public QueryBuilder insert(String table, String... columns) {

        StringBuilder fields = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");

        for (int i = 0; i < columns.length; i++) {

            String column = columns[i];
            fields.append(column);
            values.append("?");

            if (i + 1 != columns.length) {
                fields.append(",");
                values.append(",");
            }
        }

        fields.append(")");
        values.append(")");

        query.append("INSERT INTO ")
                .append(table)
                .append(" ")
                .append(fields)
                .append(" VALUES ")
                .append(values);

        return this;
    }

    public QueryBuilder delete(String table) {
        query.append("DELETE FROM ").append(table);
        return this;
    }

    public QueryBuilder updateIncrement(String table, String... columns) {
        StringBuilder fields = new StringBuilder();

        for (int i = 0; i < columns.length; i++) {

            String column = columns[i];
            fields.append(column).append("= ").append(column).append(" + ?");

            if (i + 1 != columns.length) {
                fields.append(",");
            }
        }

        query.append("UPDATE ").append(table).append(" SET ").append(fields);
        return this;
    }

    public QueryBuilder update(String table, String... columns) {
        StringBuilder fields = new StringBuilder();

        for (int i = 0; i < columns.length; i++) {

            String column = columns[i];
            fields.append(column).append("= ?");

            if (i + 1 != columns.length) {
                fields.append(",");
            }
        }

        query.append("UPDATE ").append(table).append(" SET ").append(fields);
        return this;
    }

    public void clear() {
        query.setLength(0);
    }
}
