package it.raniero.fulcrum.api.database.query;

import static org.assertj.core.api.Assertions.assertThat;

import it.raniero.fulcrum.api.database.query.utils.QueryCondition;
import org.junit.jupiter.api.Test;

class QueryBuilderTest {

    @Test
    void selectBuildsExpectedSql() {
        String sql = new QueryBuilder().select("users", "id", "name", "email").build();
        assertThat(sql).isEqualTo("SELECT id,name,email FROM users");
    }

    @Test
    void selectWithWhereUsesConditionSql() {
        QueryBuilder qb = new QueryBuilder()
                .select("users", "id", "name")
                .where(new QueryCondition("id = ?").and("name LIKE ?"));

        assertThat(qb.build()).isEqualTo("SELECT id,name FROM users WHERE id = ? AND name LIKE ?");
    }

    @Test
    void insertEmitsPlaceholders() {
        String sql = new QueryBuilder().insert("users", "name", "email").build();
        assertThat(sql).isEqualTo("INSERT INTO users (name,email) VALUES (?,?)");
    }

    @Test
    void deleteAcceptsWhere() {
        QueryBuilder qb = new QueryBuilder().delete("users").where(new QueryCondition("id = ?"));
        assertThat(qb.build()).isEqualTo("DELETE FROM users WHERE id = ?");
    }

    @Test
    void updateEmitsSetPlaceholders() {
        String sql = new QueryBuilder().update("users", "name", "email").build();
        assertThat(sql).isEqualTo("UPDATE users SET name= ?,email= ?");
    }

    @Test
    void updateIncrementReferencesColumnInExpression() {
        String sql =
                new QueryBuilder().updateIncrement("users", "score", "level").build();
        assertThat(sql).isEqualTo("UPDATE users SET score= score + ?,level= level + ?");
    }

    @Test
    void clearResetsSql() {
        QueryBuilder qb = new QueryBuilder().select("users", "id").where(new QueryCondition("id = ?"));
        qb.clear();
        assertThat(qb.build()).isEmpty();
    }

    @Test
    void whereAcceptsOrConditions() {
        QueryBuilder qb = new QueryBuilder()
                .select("users", "id")
                .where(new QueryCondition("name = ?").or("email = ?"));

        assertThat(qb.build()).isEqualTo("SELECT id FROM users WHERE name = ? OR email = ?");
    }
}
