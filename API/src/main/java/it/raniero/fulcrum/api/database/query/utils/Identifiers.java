package it.raniero.fulcrum.api.database.query.utils;

import java.util.regex.Pattern;

/**
 * SQL identifier validation. Used by {@link it.raniero.fulcrum.api.database.query.QueryBuilder}
 * and {@link QueryCondition} to ensure table and column names supplied to the query API
 * cannot be used as SQL injection vectors.
 *
 * <p>Identifiers must match {@code [A-Za-z_][A-Za-z0-9_]*}. This is intentionally stricter
 * than the SQL standard — it rejects quoted identifiers, dotted names ({@code schema.table}),
 * and anything containing whitespace or punctuation. Callers that need a qualified name
 * should validate each segment separately and join them outside this API.
 */
public final class Identifiers {

    private static final Pattern VALID = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    private Identifiers() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @throws IllegalArgumentException if {@code identifier} is null or does not match
     *     the allowed identifier pattern.
     */
    public static void validate(String identifier) {
        if (identifier == null || !VALID.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
    }

    public static void validateAll(String... identifiers) {
        for (String identifier : identifiers) {
            validate(identifier);
        }
    }
}
