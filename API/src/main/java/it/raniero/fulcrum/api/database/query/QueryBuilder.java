package it.raniero.fulcrum.api.database.query;

import it.raniero.fulcrum.api.database.query.utils.Identifiers;
import it.raniero.fulcrum.api.database.query.utils.QueryCondition;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds parameterized SQL strings against a controlled set of operations. Table and
 * column names are validated through {@link Identifiers#validate} on every call, and
 * values supplied through {@link #where(QueryCondition)} are emitted as {@code ?}
 * placeholders and collected into {@link #values()} for later binding.
 *
 * <p>Values bound to the placeholders produced by {@link #insert}, {@link #update},
 * and {@link #updateIncrement} remain the caller's responsibility: the placeholder
 * positions are explicit in the call so the caller already knows what to pass. When
 * a builder mixes those with a {@code WHERE} clause, the SET / VALUES bindings come
 * first and the {@link #values()} from the WHERE clause come last:
 *
 * <pre>{@code
 * QueryBuilder qb = new QueryBuilder()
 *         .update("users", "name")
 *         .where(QueryCondition.eq("id", 5));
 * interaction.update(qb.build(), "Alice", qb.values()[0]);
 * }</pre>
 */
public class QueryBuilder {

    private final StringBuilder query = new StringBuilder();
    private final List<Object> whereValues = new ArrayList<>();

    public String build() {
        return query.toString();
    }

    /**
     * @return values collected from {@link #where(QueryCondition)} clauses, in bind order.
     *     Empty if no WHERE clause has been added.
     */
    public Object[] values() {
        return whereValues.toArray();
    }

    public QueryBuilder select(String table, String... columns) {
        Identifiers.validate(table);
        Identifiers.validateAll(columns);

        StringBuilder fields = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            fields.append(columns[i]);
            if (i + 1 != columns.length) {
                fields.append(",");
            }
        }

        query.append("SELECT ").append(fields).append(" FROM ").append(table);
        return this;
    }

    public QueryBuilder where(QueryCondition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("WHERE condition cannot be null");
        }
        query.append(" WHERE ").append(condition.getFragment());
        whereValues.addAll(condition.getValues());
        return this;
    }

    public QueryBuilder insert(String table, String... columns) {
        Identifiers.validate(table);
        Identifiers.validateAll(columns);

        StringBuilder fields = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");

        for (int i = 0; i < columns.length; i++) {
            fields.append(columns[i]);
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
        Identifiers.validate(table);
        query.append("DELETE FROM ").append(table);
        return this;
    }

    public QueryBuilder updateIncrement(String table, String... columns) {
        Identifiers.validate(table);
        Identifiers.validateAll(columns);

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
        Identifiers.validate(table);
        Identifiers.validateAll(columns);

        StringBuilder fields = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            fields.append(columns[i]).append("= ?");
            if (i + 1 != columns.length) {
                fields.append(",");
            }
        }

        query.append("UPDATE ").append(table).append(" SET ").append(fields);
        return this;
    }

    public void clear() {
        query.setLength(0);
        whereValues.clear();
    }
}
