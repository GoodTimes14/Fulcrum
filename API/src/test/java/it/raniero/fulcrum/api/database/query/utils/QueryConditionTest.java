package it.raniero.fulcrum.api.database.query.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class QueryConditionTest {

    @Test
    void eqEmitsPlaceholderAndCollectsValue() {
        QueryCondition c = QueryCondition.eq("id", 42);
        assertThat(c.getFragment()).isEqualTo("id = ?");
        assertThat(c.getValues()).containsExactly(42);
    }

    @Test
    void comparisonsEmitTheRightOperator() {
        assertThat(QueryCondition.neq("a", 1).getFragment()).isEqualTo("a <> ?");
        assertThat(QueryCondition.gt("a", 1).getFragment()).isEqualTo("a > ?");
        assertThat(QueryCondition.gte("a", 1).getFragment()).isEqualTo("a >= ?");
        assertThat(QueryCondition.lt("a", 1).getFragment()).isEqualTo("a < ?");
        assertThat(QueryCondition.lte("a", 1).getFragment()).isEqualTo("a <= ?");
        assertThat(QueryCondition.like("a", "%foo%").getFragment()).isEqualTo("a LIKE ?");
    }

    @Test
    void isNullDoesNotBindAValue() {
        QueryCondition c = QueryCondition.isNull("deleted_at");
        assertThat(c.getFragment()).isEqualTo("deleted_at IS NULL");
        assertThat(c.getValues()).isEmpty();
    }

    @Test
    void isNotNullDoesNotBindAValue() {
        QueryCondition c = QueryCondition.isNotNull("deleted_at");
        assertThat(c.getFragment()).isEqualTo("deleted_at IS NOT NULL");
        assertThat(c.getValues()).isEmpty();
    }

    @Test
    void inEmitsOnePlaceholderPerValue() {
        QueryCondition c = QueryCondition.in("status", "ACTIVE", "PENDING", "ARCHIVED");
        assertThat(c.getFragment()).isEqualTo("status IN (?,?,?)");
        assertThat(c.getValues()).containsExactly("ACTIVE", "PENDING", "ARCHIVED");
    }

    @Test
    void inRequiresAtLeastOneValue() {
        assertThatThrownBy(() -> QueryCondition.in("status")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void andCombinesFragmentsAndValuesInOrder() {
        QueryCondition c = QueryCondition.eq("uuid", "abc").and(QueryCondition.gt("level", 5));
        assertThat(c.getFragment()).isEqualTo("(uuid = ?) AND (level > ?)");
        assertThat(c.getValues()).containsExactly("abc", 5);
    }

    @Test
    void orCombinesFragmentsAndValuesInOrder() {
        QueryCondition c = QueryCondition.eq("a", 1).or(QueryCondition.eq("b", 2));
        assertThat(c.getFragment()).isEqualTo("(a = ?) OR (b = ?)");
        assertThat(c.getValues()).containsExactly(1, 2);
    }

    @Test
    void nestedCompositionPreservesPrecedence() {
        QueryCondition c =
                QueryCondition.eq("a", 1).and(QueryCondition.eq("b", 2).or(QueryCondition.eq("c", 3)));
        assertThat(c.getFragment()).isEqualTo("(a = ?) AND ((b = ?) OR (c = ?))");
        assertThat(c.getValues()).containsExactly(1, 2, 3);
    }

    @Test
    void composingWithNullThrows() {
        assertThatThrownBy(() -> QueryCondition.eq("a", 1).and(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valueContainingSqlIsNotInjectedIntoTheFragment() {
        String malicious = "x'; DROP TABLE users; --";
        QueryCondition c = QueryCondition.eq("name", malicious);
        assertThat(c.getFragment()).isEqualTo("name = ?");
        assertThat(c.getFragment()).doesNotContain("DROP");
        assertThat(c.getValues()).containsExactly(malicious);
    }

    @Test
    void maliciousColumnNameIsRejected() {
        assertThatThrownBy(() -> QueryCondition.eq("id; DROP TABLE users; --", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid SQL identifier");
    }

    @Test
    void maliciousColumnInInIsRejected() {
        assertThatThrownBy(() -> QueryCondition.in("name) OR (1=1", "x")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getValuesIsImmutable() {
        QueryCondition c = QueryCondition.eq("a", 1);
        assertThatThrownBy(() -> c.getValues().add(2)).isInstanceOf(UnsupportedOperationException.class);
    }
}
