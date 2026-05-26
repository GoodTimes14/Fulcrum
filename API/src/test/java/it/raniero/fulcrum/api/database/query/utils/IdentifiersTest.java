package it.raniero.fulcrum.api.database.query.utils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IdentifiersTest {

    @ParameterizedTest
    @ValueSource(strings = {"users", "User_Profiles", "_internal", "a", "col1", "COLUMN_42"})
    void acceptsValidIdentifiers(String identifier) {
        assertThatCode(() -> Identifiers.validate(identifier)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "1users",
                "users; DROP TABLE x",
                "users--",
                "users WHERE 1=1",
                "users.profile",
                "`users`",
                "\"users\"",
                "users name",
                "",
                " ",
                "users'",
                "users)",
            })
    void rejectsInvalidIdentifiers(String identifier) {
        assertThatThrownBy(() -> Identifiers.validate(identifier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid SQL identifier");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> Identifiers.validate(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateAllRejectsAnyInvalidEntry() {
        assertThatThrownBy(() -> Identifiers.validateAll("good", "bad name", "alsoGood"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bad name");
    }

    @Test
    void validateAllAcceptsAllValid() {
        assertThatCode(() -> Identifiers.validateAll("a", "b", "c_2")).doesNotThrowAnyException();
    }
}
