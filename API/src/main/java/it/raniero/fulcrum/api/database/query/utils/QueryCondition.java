package it.raniero.fulcrum.api.database.query.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Type-safe SQL predicate builder. Values are always emitted as {@code ?} placeholders
 * and collected for later binding through {@link StatementUtils#createStatement}, so
 * untrusted input passed as a {@code value} cannot inject SQL. Column names are
 * validated through {@link Identifiers#validate(String)}.
 *
 * <p>Use the static factory methods to build leaf predicates, then compose with
 * {@link #and(QueryCondition)} and {@link #or(QueryCondition)}:
 *
 * <pre>{@code
 * QueryCondition c = QueryCondition.eq("uuid", playerUuid)
 *         .and(QueryCondition.gt("level", 5));
 * String fragment = c.getFragment();   // "(uuid = ?) AND (level > ?)"
 * Object[] values = c.getValues().toArray();  // [playerUuid, 5]
 * }</pre>
 */
public final class QueryCondition {

    private final StringBuilder fragment;
    private final List<Object> values;

    private QueryCondition() {
        this.fragment = new StringBuilder();
        this.values = new ArrayList<>();
    }

    public static QueryCondition eq(String column, Object value) {
        return binary(column, "=", value);
    }

    public static QueryCondition neq(String column, Object value) {
        return binary(column, "<>", value);
    }

    public static QueryCondition gt(String column, Object value) {
        return binary(column, ">", value);
    }

    public static QueryCondition gte(String column, Object value) {
        return binary(column, ">=", value);
    }

    public static QueryCondition lt(String column, Object value) {
        return binary(column, "<", value);
    }

    public static QueryCondition lte(String column, Object value) {
        return binary(column, "<=", value);
    }

    public static QueryCondition like(String column, String pattern) {
        return binary(column, "LIKE", pattern);
    }

    public static QueryCondition isNull(String column) {
        Identifiers.validate(column);
        QueryCondition c = new QueryCondition();
        c.fragment.append(column).append(" IS NULL");
        return c;
    }

    public static QueryCondition isNotNull(String column) {
        Identifiers.validate(column);
        QueryCondition c = new QueryCondition();
        c.fragment.append(column).append(" IS NOT NULL");
        return c;
    }

    public static QueryCondition in(String column, Object... values) {
        Identifiers.validate(column);
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("in() requires at least one value");
        }
        QueryCondition c = new QueryCondition();
        c.fragment.append(column).append(" IN (");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                c.fragment.append(",");
            }
            c.fragment.append("?");
            c.values.add(values[i]);
        }
        c.fragment.append(")");
        return c;
    }

    public QueryCondition and(QueryCondition other) {
        return compose("AND", other);
    }

    public QueryCondition or(QueryCondition other) {
        return compose("OR", other);
    }

    public String getFragment() {
        return fragment.toString();
    }

    public List<Object> getValues() {
        return Collections.unmodifiableList(values);
    }

    private static QueryCondition binary(String column, String op, Object value) {
        Identifiers.validate(column);
        QueryCondition c = new QueryCondition();
        c.fragment.append(column).append(' ').append(op).append(" ?");
        c.values.add(value);
        return c;
    }

    private QueryCondition compose(String operator, QueryCondition other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compose with a null condition");
        }
        // Wrap both sides in parentheses to preserve precedence regardless of how the caller
        // mixes and/or chains.
        String combined = "(" + fragment + ") " + operator + " (" + other.fragment + ')';
        fragment.setLength(0);
        fragment.append(combined);
        values.addAll(other.values);
        return this;
    }
}
