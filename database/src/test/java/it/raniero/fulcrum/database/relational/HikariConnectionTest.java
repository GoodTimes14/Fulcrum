package it.raniero.fulcrum.database.relational;

import static org.assertj.core.api.Assertions.assertThat;

import it.raniero.fulcrum.api.database.properties.ConnectionType;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.database.relational.RelationalInteraction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@EnabledIf("dockerAvailable")
class HikariConnectionTest {

    static boolean dockerAvailable() {
        if (Boolean.getBoolean("fulcrum.skipDockerTests")) {
            return false;
        }
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Container
    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0")).withDatabaseName("fulcrum_test");

    private HikariConnection connection;

    @BeforeEach
    void setUp() {
        connection = new HikariConnection();
        connection.connect(properties(), Logger.getLogger("HikariConnectionTest"));
    }

    @AfterEach
    void tearDown() {
        if (connection != null) {
            connection.close();
        }
    }

    private DatabaseProperties properties() {
        DatabaseProperties props = DatabaseProperties.builder()
                .name("test")
                .enabled(true)
                .connectionType(ConnectionType.SQL)
                .host(MYSQL.getHost())
                .port(MYSQL.getMappedPort(MySQLContainer.MYSQL_PORT))
                .database(MYSQL.getDatabaseName())
                .username(MYSQL.getUsername())
                .password(MYSQL.getPassword())
                .build();
        return props;
    }

    @Test
    void connectExposesAUsableConnection() throws SQLException {
        try (Connection conn = connection.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isValid(2)).isTrue();
        }
        assertThat(connection.getLastActionTime()).isPositive();
    }

    @Test
    void queryAndUpdateRoundTrip() throws Exception {
        try (RelationalInteraction interaction = connection.createInteraction(true)) {
            interaction.update("CREATE TABLE IF NOT EXISTS items (id INT PRIMARY KEY, label VARCHAR(64))");
            interaction.update("INSERT INTO items (id, label) VALUES (?, ?)", 1, "alpha");
            interaction.update("INSERT INTO items (id, label) VALUES (?, ?)", 2, "beta");

            try (ResultSet rs = interaction.query("SELECT label FROM items WHERE id = ?", 1)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("label")).isEqualTo("alpha");
                assertThat(rs.next()).isFalse();
            }
        }
    }

    @Test
    void parameterBindingPreventsSqlInjection() throws Exception {
        try (RelationalInteraction interaction = connection.createInteraction(true)) {
            interaction.update("CREATE TABLE IF NOT EXISTS sec_items (id INT PRIMARY KEY, label VARCHAR(64))");
            interaction.update("INSERT INTO sec_items (id, label) VALUES (?, ?)", 1, "alpha");

            String malicious = "alpha' OR '1'='1";
            try (ResultSet rs = interaction.query("SELECT id FROM sec_items WHERE label = ?", malicious)) {
                assertThat(rs.next())
                        .as("bound parameter must be treated as literal, not SQL")
                        .isFalse();
            }
        }
    }

    @Test
    void rollbackUndoesPendingChanges() throws Exception {
        try (RelationalInteraction setup = connection.createInteraction(true)) {
            setup.update("CREATE TABLE IF NOT EXISTS tx_items (id INT PRIMARY KEY)");
            setup.update("DELETE FROM tx_items");
        }

        try (RelationalInteraction tx = connection.createInteraction(false)) {
            tx.update("INSERT INTO tx_items (id) VALUES (?)", 99);
            tx.rollback();
        }

        try (RelationalInteraction check = connection.createInteraction(true);
                ResultSet rs = check.query("SELECT COUNT(*) FROM tx_items")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isZero();
        }
    }

    @Test
    void safeCommitReturnsTrueOnSuccessAndPersists() throws Exception {
        try (RelationalInteraction setup = connection.createInteraction(true)) {
            setup.update("CREATE TABLE IF NOT EXISTS commit_items (id INT PRIMARY KEY)");
            setup.update("DELETE FROM commit_items");
        }

        try (RelationalInteraction tx = connection.createInteraction(false)) {
            tx.update("INSERT INTO commit_items (id) VALUES (?)", 7);
            assertThat(tx.safeCommit()).isTrue();
        }

        try (RelationalInteraction check = connection.createInteraction(true);
                ResultSet rs = check.query("SELECT id FROM commit_items")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(7);
        }
    }

    @Test
    void asyncInteractionExecutesAndCompletes() throws Exception {
        try (RelationalInteraction setup = connection.createInteraction(true)) {
            setup.update("CREATE TABLE IF NOT EXISTS async_items (id INT PRIMARY KEY)");
            setup.update("DELETE FROM async_items");
        }

        CompletableFuture<Void> future = connection.asyncInteraction(interaction -> {
            try {
                interaction.autoCommit(false);
                interaction.update("INSERT INTO async_items (id) VALUES (?)", 1);
                interaction.update("INSERT INTO async_items (id) VALUES (?)", 2);
                interaction.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        future.get(10, TimeUnit.SECONDS);

        try (RelationalInteraction check = connection.createInteraction(true);
                ResultSet rs = check.query("SELECT COUNT(*) FROM async_items")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(2);
        }
    }

    @Test
    void propertiesAreExposedAfterConnect() {
        assertThat(connection.properties()).isNotNull();
        assertThat(connection.properties().getDatabase()).isEqualTo(MYSQL.getDatabaseName());
        assertThat(connection.properties().getUsername()).isEqualTo(MYSQL.getUsername());
    }
}
