package it.raniero.fulcrum.database.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.lettuce.core.RedisFuture;
import it.raniero.fulcrum.api.database.loqui.ILoqui;
import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryData;
import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryOptions;
import it.raniero.fulcrum.api.database.loqui.message.LoquiContent;
import it.raniero.fulcrum.api.database.loqui.message.LoquiEnvelope;
import it.raniero.fulcrum.api.database.loqui.message.LoquiMessage;
import it.raniero.fulcrum.api.database.properties.ConnectionType;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import it.raniero.fulcrum.api.database.redis.Listen;
import it.raniero.fulcrum.api.database.redis.RedisListener;
import it.raniero.fulcrum.database.redis.loqui.discovery.RedisLoquiDiscoveryStore;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@EnabledIf("dockerAvailable")
class LettuceConnectionTest {

    private static final int REDIS_PORT = 6379;

    private static final String LOQUI_CHANNEL = "loqui-channel";

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
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(REDIS_PORT);

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

        assertThat(connection.cache().keys("session:*")).containsExactlyInAnyOrder("session:a", "session:b");
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
    void loquiAdvertisementCanBeDiscoveredAndReleased() {
        String name = "lobby-" + UUID.randomUUID();

        assertThat(connection.getLoqui().discovery().advertise(name)).isTrue();

        LoquiDiscoveryData discovered =
                connection.getLoqui().discovery().discover(name).orElseThrow();
        assertThat(discovered.senderId()).isEqualTo(connection.getLoqui().getSenderId());
        assertThat(connection.cache().fetch(discoveryKey(name))).isEqualTo(discovered.serialize());
        assertThat(connection.getInteractionConnection().sync().ttl(discoveryKey(name)))
                .isPositive()
                .isLessThanOrEqualTo(discovered.updateTTL());
        assertThat(connection.getLoqui().discovery().discoverAll()).containsEntry(name, discovered);
        assertThat(connection.getLoqui().discovery().getAdvertisements()).containsExactly(name);

        assertThat(connection.getLoqui().discovery().stopAdvertising(name)).isTrue();
        assertThat(connection.getLoqui().discovery().discover(name)).isEmpty();
    }

    @Test
    void liveForeignAdvertisementRequiresAggressiveRetake() {
        String name = "proxy-" + UUID.randomUUID();
        UUID foreignSender = UUID.randomUUID();
        putDiscoveryRecord(name, foreignSender, 30);

        assertThat(connection.getLoqui().discovery().advertise(name)).isFalse();
        assertThat(connection.getLoqui().discovery().discover(name))
                .get()
                .extracting(LoquiDiscoveryData::senderId)
                .isEqualTo(foreignSender);

        connection.getLoqui().discovery().setOptions(new LoquiDiscoveryOptions(10, 30, true));
        assertThat(connection.getLoqui().discovery().advertise(name)).isTrue();
        assertThat(connection.getLoqui().discovery().discover(name))
                .get()
                .extracting(LoquiDiscoveryData::senderId)
                .isEqualTo(connection.getLoqui().getSenderId());
    }

    @Test
    void formerOwnerCannotDeleteAReplacementAdvertisement() {
        String name = "gateway-" + UUID.randomUUID();
        UUID replacement = UUID.randomUUID();
        assertThat(connection.getLoqui().discovery().advertise(name)).isTrue();

        LoquiDiscoveryData replacementData = LoquiDiscoveryData.current(replacement, System.currentTimeMillis(), 30);
        connection.cache().set(discoveryKey(name), replacementData.serialize(), replacementData.updateTTL());

        assertThat(connection.getLoqui().discovery().stopAdvertising(name)).isFalse();
        assertThat(LoquiDiscoveryData.deserialize(connection.cache().fetch(discoveryKey(name)))
                        .senderId())
                .isEqualTo(replacement);
    }

    @Test
    void malformedDiscoveryKeysAreIgnored() {
        String name = "malformed-" + UUID.randomUUID();
        connection.cache().set(discoveryKey(name), "not-a-discovery-record", 30);

        assertThat(connection.getLoqui().discovery().discover(name)).isEmpty();
        assertThat(connection.getLoqui().discovery().discoverAll()).doesNotContainKey(name);
    }

    @Test
    void unrefreshedDiscoveryAdvertisementExpiresFromRedis() {
        String name = "expired-" + UUID.randomUUID();
        RedisLoquiDiscoveryStore store = new RedisLoquiDiscoveryStore(connection);
        store.set(name, LoquiDiscoveryData.current(UUID.randomUUID(), System.currentTimeMillis(), 1));

        assertThat(connection.getInteractionConnection().sync().ttl(discoveryKey(name)))
                .isPositive();
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> connection.getInteractionConnection().sync().exists(discoveryKey(name)) == 0);
    }

    @Test
    void discoveryHeartbeatKeepsTheRedisLeaseAlive() {
        String name = "heartbeat-" + UUID.randomUUID();
        connection.getLoqui().discovery().setOptions(new LoquiDiscoveryOptions(1, 2, false));
        connection.getLoqui().discovery().advertise(name);

        await().pollDelay(Duration.ofMillis(2_500))
                .atMost(Duration.ofSeconds(4))
                .untilAsserted(() -> {
                    assertThat(connection.getLoqui().discovery().discover(name)).isPresent();
                    assertThat(connection.getInteractionConnection().sync().ttl(discoveryKey(name)))
                            .isPositive();
                });
    }

    @Test
    void closingLoquiReleasesItsAdvertisements() {
        String name = "shutdown-" + UUID.randomUUID();
        connection.getLoqui().discovery().advertise(name);

        connection.getLoqui().discovery().close();

        assertThat(connection.getInteractionConnection().sync().exists(discoveryKey(name)))
                .isZero();
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
        await().pollDelay(Duration.ofMillis(300)).atMost(Duration.ofSeconds(2)).until(() -> true);

        assertThat(listener.received).isEmpty();
    }

    @Test
    void loquiMessageIsDecodedAndDispatchedToATypedListener() {
        LoquiRecordingListener listener = registerLoquiListener();

        connection.publish(LOQUI_CHANNEL, remoteEnvelope(ILoqui.BROADCAST, "hello"));

        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(50))
                .until(() -> !listener.received.isEmpty());

        assertThat(listener.received.peek().content).isEqualTo("hello");
        assertThat(listener.rawReceived).isEmpty();
    }

    @Test
    void loquiMessageAddressedToAnotherInstanceIsIgnored() {
        LoquiRecordingListener listener = registerLoquiListener();

        connection.publish(LOQUI_CHANNEL, remoteEnvelope(UUID.randomUUID().toString(), "hello"));

        await().pollDelay(Duration.ofMillis(300)).atMost(Duration.ofSeconds(2)).until(() -> true);

        assertThat(listener.received).isEmpty();
    }

    @Test
    void ownLoquiMessagesAreNotEchoedBack() {
        LoquiRecordingListener listener = registerLoquiListener();

        connection.getLoqui().broadcastMessage(LOQUI_CHANNEL, new PingMessage("hello"));

        await().pollDelay(Duration.ofMillis(300)).atMost(Duration.ofSeconds(2)).until(() -> true);

        assertThat(listener.received).isEmpty();
    }

    @Test
    void malformedLoquiMessageDoesNotReachListeners() {
        LoquiRecordingListener listener = registerLoquiListener();

        connection.publish(LOQUI_CHANNEL, "not-an-envelope");

        await().pollDelay(Duration.ofMillis(300)).atMost(Duration.ofSeconds(2)).until(() -> true);

        assertThat(listener.received).isEmpty();
        assertThat(listener.rawReceived).isEmpty();
    }

    @Test
    void loquiMessageAddressedToAJoinedMulticastGroupIsDispatched() {
        LoquiRecordingListener listener = registerLoquiListener();
        connection.getLoqui().registerMulticastGroup(LOQUI_CHANNEL, "lobbies");
        connection.getLoqui().registerMulticastGroup(LOQUI_CHANNEL, "eu-west");

        connection.publish(LOQUI_CHANNEL, remoteEnvelope("eu-west", "hello"));

        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(50))
                .until(() -> !listener.received.isEmpty());

        assertThat(listener.received.peek().content).isEqualTo("hello");
    }

    @Test
    void loquiMessageAddressedToAGroupThatWasNotJoinedIsIgnored() {
        LoquiRecordingListener listener = registerLoquiListener();
        connection.getLoqui().registerMulticastGroup(LOQUI_CHANNEL, "lobbies");

        connection.publish(LOQUI_CHANNEL, remoteEnvelope("minigames", "hello"));

        await().pollDelay(Duration.ofMillis(300)).atMost(Duration.ofSeconds(2)).until(() -> true);

        assertThat(listener.received).isEmpty();
        assertThat(listener.envelopes).isEmpty();
    }

    @Test
    void multicastGroupsJoinedOnAnotherChannelDoNotApply() {
        LoquiRecordingListener listener = registerLoquiListener();
        connection.getLoqui().registerMulticastGroup("another-channel", "lobbies");

        connection.publish(LOQUI_CHANNEL, remoteEnvelope("lobbies", "hello"));

        await().pollDelay(Duration.ofMillis(300)).atMost(Duration.ofSeconds(2)).until(() -> true);

        assertThat(listener.received).isEmpty();
    }

    @Test
    void listenersCanTakeTheEnvelopeToReadItsMetadata() {
        LoquiRecordingListener listener = registerLoquiListener();
        connection.getLoqui().registerMulticastGroup(LOQUI_CHANNEL, "lobbies");

        String serialized = remoteEnvelope("lobbies", "hello");
        connection.publish(LOQUI_CHANNEL, serialized);

        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(50))
                .until(() -> !listener.envelopes.isEmpty());

        LoquiEnvelope envelope = listener.envelopes.peek();
        assertThat(envelope.target()).isEqualTo("lobbies");
        assertThat(envelope.packetId()).isEqualTo("ping_message");
        assertThat(envelope.from())
                .isEqualTo(LoquiEnvelope.deserialize(serialized).from());
        assertThat(listener.rawReceived).isEmpty();
    }

    @Test
    void envelopeListenersAreServedEvenWithoutADecoder() {
        LoquiRecordingListener listener = registerLoquiListener();

        connection.publish(LOQUI_CHANNEL, remoteEnvelope(ILoqui.BROADCAST, "unknown_message", "hello"));

        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(50))
                .until(() -> !listener.envelopes.isEmpty());

        assertThat(listener.envelopes.peek().packetId()).isEqualTo("unknown_message");
        assertThat(listener.received).isEmpty();
    }

    private LoquiRecordingListener registerLoquiListener() {
        LoquiRecordingListener listener = new LoquiRecordingListener();
        connection.registerListener(listener);
        connection.getLoqui().registerMessageType(PingMessage.class, PingMessage::new);
        connection.getLoqui().registerChannel(LOQUI_CHANNEL);

        return listener;
    }

    private String remoteEnvelope(String target, String content) {
        return remoteEnvelope(target, "ping_message", content);
    }

    private String remoteEnvelope(String target, String packetId, String content) {
        return new LoquiEnvelope(target, UUID.randomUUID().toString(), packetId, "{\"content\":\"" + content + "\"}")
                .serialize();
    }

    private void putDiscoveryRecord(String name, UUID senderId, long ttl) {
        LoquiDiscoveryData data = LoquiDiscoveryData.current(senderId, System.currentTimeMillis(), ttl);
        connection.cache().set(discoveryKey(name), data.serialize(), data.updateTTL());
    }

    private String discoveryKey(String name) {
        return "fulcrum:loqui:discovery:" + name;
    }

    static class RecordingListener implements RedisListener {
        final LinkedBlockingQueue<String> received = new LinkedBlockingQueue<>();

        @Listen(channel = "test-channel")
        public void onTest(String message) {
            received.add(message);
        }
    }

    static class LoquiRecordingListener implements RedisListener {
        final LinkedBlockingQueue<PingMessage> received = new LinkedBlockingQueue<>();
        final LinkedBlockingQueue<LoquiEnvelope> envelopes = new LinkedBlockingQueue<>();
        final LinkedBlockingQueue<String> rawReceived = new LinkedBlockingQueue<>();

        @Listen(channel = LOQUI_CHANNEL)
        public void onPing(PingMessage message) {
            received.add(message);
        }

        @Listen(channel = LOQUI_CHANNEL)
        public void onEnvelope(LoquiEnvelope envelope) {
            envelopes.add(envelope);
        }

        @Listen(channel = LOQUI_CHANNEL)
        public void onRaw(String message) {
            rawReceived.add(message);
        }
    }

    static class PingMessage extends LoquiMessage {

        String content;

        PingMessage(String content) {
            this.content = content;
        }

        PingMessage() {
            super();
        }

        @Override
        public void deserialize(LoquiContent content) {
            this.content = content.getString("content");
        }
    }
}
