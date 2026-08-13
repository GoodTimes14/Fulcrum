package it.raniero.fulcrum.database.redis.loqui.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryData;
import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryOptions;
import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class FulcrumLoquiDiscoveryTest {

    private static final long NOW = 10_000;

    private final List<FulcrumLoquiDiscovery> discoveries = new ArrayList<>();

    @AfterEach
    void tearDown() {
        discoveries.forEach(FulcrumLoquiDiscovery::close);
    }

    @Test
    void advertisementCanBeDiscoveredAndReleased() {
        FakeStore store = new FakeStore();
        UUID senderId = UUID.randomUUID();
        FulcrumLoquiDiscovery discovery = discovery(store, senderId);

        assertThat(discovery.advertise("lobby-one")).isTrue();
        assertThat(discovery.getAdvertisements()).containsExactly("lobby-one");
        assertThat(discovery.discover("lobby-one"))
                .get()
                .extracting(LoquiDiscoveryData::senderId)
                .isEqualTo(senderId);

        assertThat(discovery.stopAdvertising("lobby-one")).isTrue();
        assertThat(discovery.discover("lobby-one")).isEmpty();
        assertThat(discovery.getAdvertisements()).isEmpty();
    }

    @Test
    void liveAdvertisementCannotBeStolenWithoutAggressiveRetake() {
        FakeStore store = new FakeStore();
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        FulcrumLoquiDiscovery first = discovery(store, firstId);
        FulcrumLoquiDiscovery second = discovery(store, secondId);

        assertThat(first.advertise("proxy")).isTrue();
        assertThat(second.advertise("proxy")).isFalse();

        second.setOptions(new LoquiDiscoveryOptions(10, 20, true));
        assertThat(second.advertise("proxy")).isTrue();
        assertThat(second.discover("proxy"))
                .get()
                .extracting(LoquiDiscoveryData::senderId)
                .isEqualTo(secondId);

        // The discovery layer checks the stored owner before deleting.
        assertThat(first.stopAdvertising("proxy")).isFalse();
        assertThat(second.discover("proxy")).isPresent();
    }

    @Test
    void expiredMalformedAndUnsupportedRecordsAreNotDiscovered() {
        FakeStore store = new FakeStore();
        FulcrumLoquiDiscovery discovery = discovery(store, UUID.randomUUID());
        store.put("expired", LoquiDiscoveryData.current(UUID.randomUUID(), NOW - 2_000, 1));
        store.put("future-version", new LoquiDiscoveryData(2, UUID.randomUUID(), NOW, 10));

        assertThat(discovery.discover("expired")).isEmpty();
        assertThat(discovery.discover("future-version")).isEmpty();
        assertThat(discovery.discoverAll()).isEmpty();
    }

    @Test
    void invalidNamesAndTimingOptionsAreRejected() {
        FulcrumLoquiDiscovery discovery = discovery(new FakeStore(), UUID.randomUUID());

        assertThatThrownBy(() -> discovery.advertise(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> discovery.advertise("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> discovery.discover("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> discovery.setOptions(new LoquiDiscoveryOptions(10, 19, false)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void heartbeatRefreshesAnOwnedAdvertisement() {
        FakeStore store = new FakeStore();
        FulcrumLoquiDiscovery discovery = discovery(store, UUID.randomUUID());
        discovery.setOptions(new LoquiDiscoveryOptions(1, 2, false));

        discovery.advertise("heartbeat");

        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> assertThat(store.writes()).isGreaterThanOrEqualTo(2));
    }

    @Test
    void closeReleasesOnlyAdvertisementsStillOwnedByThisSender() {
        FakeStore store = new FakeStore();
        FulcrumLoquiDiscovery first = discovery(store, UUID.randomUUID());
        FulcrumLoquiDiscovery second = discovery(store, UUID.randomUUID());
        first.advertise("gateway");
        second.setOptions(new LoquiDiscoveryOptions(10, 20, true));
        second.advertise("gateway");

        first.close();

        assertThat(second.discover("gateway")).isPresent();
        second.close();
        assertThat(store.get("gateway")).isEmpty();
    }

    private FulcrumLoquiDiscovery discovery(FakeStore store, UUID senderId) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.OFF);
        FulcrumLoquiDiscovery discovery = new FulcrumLoquiDiscovery(
                store,
                senderId,
                Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC),
                Executors.newSingleThreadScheduledExecutor(),
                logger);
        discoveries.add(discovery);
        return discovery;
    }

    private static final class FakeStore implements LoquiDiscoveryStore {

        private final Map<String, LoquiDiscoveryData> records = new HashMap<>();
        private final AtomicInteger writes = new AtomicInteger();

        @Override
        public synchronized void set(String name, LoquiDiscoveryData data) {
            writes.incrementAndGet();
            records.put(name, data);
        }

        @Override
        public synchronized Optional<LoquiDiscoveryData> get(String name) {
            return Optional.ofNullable(records.get(name));
        }

        @Override
        public synchronized Map<String, LoquiDiscoveryData> getAll() {
            return Map.copyOf(records);
        }

        @Override
        public synchronized void delete(String name) {
            records.remove(name);
        }

        synchronized void put(String name, LoquiDiscoveryData data) {
            records.put(name, data);
        }

        int writes() {
            return writes.get();
        }
    }
}
