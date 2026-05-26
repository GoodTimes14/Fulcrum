package it.raniero.fulcrum.api.database.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import it.raniero.fulcrum.api.database.query.utils.QueryCondition;
import org.junit.jupiter.api.Test;

class QueryBuilderTest {

    @Test
    void selectBuildsExpectedSql() {
        String sql = new QueryBuilder().select("users", "id", "name", "email").build();
        assertThat(sql).isEqualTo("SELECT id,name,email FROM users");
    }

    @Test
    void selectWithWhereCollectsBoundValues() {
        QueryBuilder qb = new QueryBuilder()
                .select("users", "id", "name")
                .where(QueryCondition.eq("id", 5).and(QueryCondition.like("name", "A%")));

        assertThat(qb.build()).isEqualTo("SELECT id,name FROM users WHERE (id = ?) AND (name LIKE ?)");
        assertThat(qb.values()).containsExactly(5, "A%");
    }

    @Test
    void insertEmitsPlaceholders() {
        String sql = new QueryBuilder().insert("users", "name", "email").build();
        assertThat(sql).isEqualTo("INSERT INTO users (name,email) VALUES (?,?)");
    }

    @Test
    void deleteAcceptsWhere() {
        QueryBuilder qb = new QueryBuilder().delete("users").where(QueryCondition.eq("id", 99));
        assertThat(qb.build()).isEqualTo("DELETE FROM users WHERE id = ?");
        assertThat(qb.values()).containsExactly(99);
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
    void valuesIsEmptyWhenNoWhereClauseAdded() {
        QueryBuilder qb = new QueryBuilder().select("users", "id");
        assertThat(qb.values()).isEmpty();
    }

    @Test
    void clearResetsBothSqlAndValues() {
        QueryBuilder qb = new QueryBuilder().select("users", "id").where(QueryCondition.eq("id", 1));
        qb.clear();
        assertThat(qb.build()).isEmpty();
        assertThat(qb.values()).isEmpty();
    }

    // --- Injection attempts: every malicious identifier must throw ---

    @Test
    void selectRejectsInjectedTableName() {
        assertThatThrownBy(() -> new QueryBuilder().select("users; DROP TABLE x", "id"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void selectRejectsInjectedColumnName() {
        assertThatThrownBy(() -> new QueryBuilder().select("users", "id, password"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void insertRejectsInjectedTableName() {
        assertThatThrownBy(() -> new QueryBuilder().insert("users) VALUES (1); --", "name"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void insertRejectsInjectedColumnName() {
        assertThatThrownBy(() -> new QueryBuilder().insert("users", "name`,`admin"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteRejectsInjectedTableName() {
        assertThatThrownBy(() -> new QueryBuilder().delete("users WHERE 1=1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsInjectedTableName() {
        assertThatThrownBy(() -> new QueryBuilder().update("users; --", "name"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsInjectedColumnName() {
        assertThatThrownBy(() -> new QueryBuilder().update("users", "name= ?; DROP TABLE x; --"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateIncrementRejectsInjectedColumnName() {
        assertThatThrownBy(() -> new QueryBuilder().updateIncrement("users", "score'; DROP"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whereRejectsNullCondition() {
        assertThatThrownBy(() -> new QueryBuilder().select("users", "id").where(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void maliciousValueRoutedThroughPlaceholderNeverAppearsInSql() {
        String malicious = "'; DROP TABLE users; --";
        QueryBuilder qb = new QueryBuilder().select("users", "id").where(QueryCondition.eq("name", malicious));

        assertThat(qb.build()).isEqualTo("SELECT id FROM users WHERE name = ?");
        assertThat(qb.build()).doesNotContain("DROP");
        assertThat(qb.values()).containsExactly(malicious);
    }
}
