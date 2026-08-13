package it.raniero.fulcrum.api.database.loqui.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoquiDiscoveryOptionsTest {

    @Test
    void defaultsAreValid() {
        assertThat(LoquiDiscoveryOptions.areOptionsValid(LoquiDiscoveryOptions.defaultOptions()))
                .isTrue();
    }

    @Test
    void ttlMustCoverAtLeastTwoRefreshIntervals() {
        assertThat(LoquiDiscoveryOptions.areOptionsValid(new LoquiDiscoveryOptions(10, 20, false)))
                .isTrue();
        assertThat(LoquiDiscoveryOptions.areOptionsValid(new LoquiDiscoveryOptions(10, 19, false)))
                .isFalse();
    }

    @Test
    void nullZeroAndNegativeValuesAreInvalid() {
        assertThat(LoquiDiscoveryOptions.areOptionsValid(null)).isFalse();
        assertThat(LoquiDiscoveryOptions.areOptionsValid(new LoquiDiscoveryOptions(0, 20, false)))
                .isFalse();
        assertThat(LoquiDiscoveryOptions.areOptionsValid(new LoquiDiscoveryOptions(10, 0, false)))
                .isFalse();
        assertThat(LoquiDiscoveryOptions.areOptionsValid(new LoquiDiscoveryOptions(-1, 20, false)))
                .isFalse();
    }

    @Test
    void validationDoesNotOverflowForLargeValues() {
        assertThat(LoquiDiscoveryOptions.areOptionsValid(
                        new LoquiDiscoveryOptions(Long.MAX_VALUE, Long.MAX_VALUE, false)))
                .isFalse();
    }
}
