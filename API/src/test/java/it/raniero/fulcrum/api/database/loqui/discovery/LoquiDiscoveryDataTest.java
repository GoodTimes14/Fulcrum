package it.raniero.fulcrum.api.database.loqui.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class LoquiDiscoveryDataTest {

    @Test
    void currentRecordUsesTheSupportedProtocolVersion() {
        LoquiDiscoveryData data = LoquiDiscoveryData.current(UUID.randomUUID(), 1_000, 15);

        assertThat(data.version()).isEqualTo(LoquiDiscoveryData.CURRENT_VERSION);
    }

    @Test
    void expirationUsesEpochMillisecondsAndASecondsTtl() {
        LoquiDiscoveryData data = LoquiDiscoveryData.current(UUID.randomUUID(), 10_000, 5);

        assertThat(data.expiresAt()).isEqualTo(15_000);
        assertThat(data.isExpired(14_999)).isFalse();
        assertThat(data.isExpired(15_000)).isTrue();
    }

    @Test
    void invalidRecordsAreRejected() {
        UUID senderId = UUID.randomUUID();

        assertThatThrownBy(() -> new LoquiDiscoveryData(0, senderId, 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LoquiDiscoveryData(1, null, 1, 1)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LoquiDiscoveryData(1, senderId, -1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LoquiDiscoveryData(1, senderId, 1, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void expirationCalculationSaturatesInsteadOfOverflowing() {
        LoquiDiscoveryData data = new LoquiDiscoveryData(1, UUID.randomUUID(), Long.MAX_VALUE - 1, Long.MAX_VALUE);

        assertThat(data.expiresAt()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void serializedRecordRoundTrips() {
        LoquiDiscoveryData data = LoquiDiscoveryData.current(UUID.randomUUID(), 10_000, 45);

        assertThat(LoquiDiscoveryData.deserialize(data.serialize())).isEqualTo(data);
    }

    @Test
    void malformedSerializedRecordsAreRejected() {
        assertThatThrownBy(() -> LoquiDiscoveryData.deserialize(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LoquiDiscoveryData.deserialize("1|missing-fields"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LoquiDiscoveryData.deserialize("one|not-a-uuid|three|four"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
