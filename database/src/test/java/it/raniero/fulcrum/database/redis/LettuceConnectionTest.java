package it.raniero.fulcrum.database.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.lettuce.core.RedisFuture;
import it.raniero.fulcrum.api.database.properties.ConnectionType;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.database.redis.Listen;
import it.raniero.fulcrum.api.database.redis.RedisListener;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class LettuceConnectionTest {

    private static final int REDIS_PORT = 6379;

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(REDIS_PORT);

    private LettuceConnection connection;

    @BeforeEach
    void setUp() {
        connection = new LettuceConnection(Logger.getLogger("LettuceConnectionTest"), properties());
    }

    @AfterEach
    void tearDown() {
        if (connection != null) {
            connection.close();
        }
    }

    private DatabaseProperties properties() {
        return DatabaseProperties.builder()
                .name("redis-test")
                .enabled(true)
                .connectionType(ConnectionType.REDIS)
                .host(REDIS.getHost())
                .port(REDIS.getMappedPort(REDIS_PORT))
                .build();
    }

    @Test
    void setAndFetchRoundTripsAStringValue() {
        connection.cache().set("greeting", "hello");

        assertThat(connection.cache().fetch("greeting")).isEqualTo("hello");
        assertThat(connection.cache().exists("greeting")).isTrue();
    }

    @Test
    void fetchReturnsEmptyStringWhenKeyMissing() {
        assertThat(connection.cache().fetch("never-set")).isEmpty();
        assertThat(connection.cache().exists("never-set")).isFalse();
    }

    @Test
    void deleteRemovesAKey() {
        connection.cache().set("to-delete", "value");
        assertThat(connection.cache().exists("to-delete")).isTrue();

        connection.cache().delete("to-delete");

        assertThat(connection.cache().exists("to-delete")).isFalse();
    }

    @Test
    void hashMapOperationsRoundTrip() {
        connection.cache().insertMap("user:1", Map.of("name", "raniero", "role", "admin"));
        connection.cache().updateMap("user:1", "role", "owner");

        Map<String, String> stored = connection.cache().getMap("user:1");

        assertThat(stored).containsEntry("name", "raniero").containsEntry("role", "owner");
    }

    @Test
    void keysReturnsMatchingPattern() {
        connection.cache().set("session:a", "1");
        connection.cache().set("session:b", "2");
        connection.cache().set("other:c", "3");

        assertThat(connection.cache().keys("session:*"))
                .containsExactlyInAnyOrder("session:a", "session:b");
    }

    @Test
    void initExpireMakesKeyDisappear() {
        connection.cache().set("ephemeral", "x");
        connection.cache().initExpire("ephemeral", 1);

        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(200))
                .until(() -> !connection.cache().exists("ephemeral"));
    }

    @Test
    void asyncSetAndGetRoundTrip() throws Exception {
        RedisFuture<String> setFuture = connection.asyncCache().set("async-key", "async-value");
        setFuture.get(5, TimeUnit.SECONDS);

        RedisFuture<String> getFuture = connection.asyncCache().get("async-key");
        assertThat(getFuture.get(5, TimeUnit.SECONDS)).isEqualTo("async-value");
    }

    @Test
    void publishedMessageIsDispatchedToAnnotatedListener() {
        RecordingListener listener = new RecordingListener();
        connection.registerListener(listener);
        connection.subscribe("test-channel");

        connection.publish("test-channel", "ping");

        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(50))
                .until(() -> !listener.received.isEmpty());

        assertThat(listener.received).containsExactly("ping");
    }

    @Test
    void listenersBoundToOtherChannelsDoNotReceiveTheMessage() {
        RecordingListener listener = new RecordingListener();
        connection.registerListener(listener);
        connection.subscribe("test-channel");
        connection.subscribe("other-channel");

        connection.publish("other-channel", "noise");

        // No interrupt — give the listener time to *not* receive on test-channel.
        await().pollDelay(Duration.ofMillis(300))
                .atMost(Duration.ofSeconds(2))
                .until(() -> true);

        assertThat(listener.received).isEmpty();
    }

    static class RecordingListener implements RedisListener {
        final LinkedBlockingQueue<String> received = new LinkedBlockingQueue<>();

        @Listen(channel = "test-channel")
        public void onTest(String message) {
            received.add(message);
        }
    }
}