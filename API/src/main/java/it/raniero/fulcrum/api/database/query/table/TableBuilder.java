package it.raniero.fulcrum.api.database.query.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TableBuilder {

    private final String table;

    private final StringBuilder fields = new StringBuilder();

    private final StringBuilder constraints = new StringBuilder();

    private boolean exists = false;

    public TableBuilder field(String name, String type, int size, String extra) {

        if (!fields.isEmpty()) {
            fields.append(",");
        }

        fields.append(name).append(" ").append(type.toUpperCase());

        if (size > 0) {
            fields.append("(").append(size).append(")");
        }

        if (extra != null) {
            fields.append(" ").append(extra);
        }

        return this;
    }

    public TableBuilder field(String name, String type, int size) {
        field(name, type, size, null);
        return this;
    }

    public TableBuilder field(String name, String type) {
        field(name, type, -1, null);
        return this;
    }

    public TableBuilder field(String name, String type, String extra) {
        field(name, type, -1, extra);
        return this;
    }

    public TableBuilder ifNotExists(boolean exists) {
        this.exists = exists;
        return this;
    }

    public TableBuilder primaryKey(String name) {

        if (!constraints.isEmpty()) {
            constraints.append(",");
        }

        constraints.append("PRIMARY KEY(").append(name).append(")");

        return this;
    }

    public TableBuilder foreignKey(String name, String reference, String onUpdate, String onDelete) {

        if (!constraints.isEmpty()) {
            constraints.append(",");
        }

        constraints.append("FOREIGN KEY(").append(name).append(") REFERENCES ").append(reference);

        if (onUpdate != null) {
            constraints.append(" ON UPDATE ").append(onUpdate);
        }

        if (onDelete != null) {
            constraints.append(" ON DELETE ").append(onDelete);
        }

        return this;
    }

    public TableBuilder unique(String name) {

        if (!constraints.isEmpty()) {
            constraints.append(",");
        }

        constraints.append("UNIQUE(").append(name).append(")");

        return this;
    }

    public String build() {

        StringBuilder output = new StringBuilder();

        output.append("CREATE TABLE ");

        if (exists) {
            output.append("IF NOT EXISTS ");
        }

        output.append(table).append(" (").append(fields);

        if (!constraints.isEmpty()) {
            output.append(",").append(constraints);
        }

        output.append(")");

        return output + ";";
    }
}
