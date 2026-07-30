package it.raniero.fulcrum.api.database.loqui.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import it.raniero.fulcrum.api.database.loqui.exception.LoquiDecoderException;
import it.raniero.fulcrum.api.database.loqui.exception.LoquiEncodeException;
import org.junit.jupiter.api.Test;

class LoquiEnvelopeTest {

    @Test
    void serializeAndDeserializeRoundTrip() {
        LoquiEnvelope envelope = new LoquiEnvelope("target", "sender", "ping_message", "{\"content\":\"hi\"}");

        assertThat(LoquiEnvelope.deserialize(envelope.serialize())).isEqualTo(envelope);
    }

    @Test
    void payloadKeepsItsSeparators() {
        LoquiEnvelope envelope = new LoquiEnvelope("target", "sender", "ping_message", "{\"content\":\"a|b|c\"}");

        LoquiEnvelope decoded = LoquiEnvelope.deserialize(envelope.serialize());

        assertThat(decoded.serializedMessage()).isEqualTo("{\"content\":\"a|b|c\"}");
    }

    @Test
    void emptyPayloadSurvivesTheRoundTrip() {
        LoquiEnvelope envelope = new LoquiEnvelope("target", "sender", "ping_message", "");

        assertThat(LoquiEnvelope.deserialize(envelope.serialize()).serializedMessage())
                .isEmpty();
    }

    @Test
    void deserializeRejectsTruncatedMessages() {
        assertThatThrownBy(() -> LoquiEnvelope.deserialize("target|sender|ping_message"))
                .isInstanceOf(LoquiDecoderException.class);

        assertThatThrownBy(() -> LoquiEnvelope.deserialize(null)).isInstanceOf(LoquiDecoderException.class);
    }

    @Test
    void deserializeRejectsEmptyHeaderFields() {
        assertThatThrownBy(() -> LoquiEnvelope.deserialize("target||ping_message|{}"))
                .isInstanceOf(LoquiDecoderException.class);
    }

    @Test
    void headerFieldsCanNotHoldASeparator() {
        assertThatThrownBy(() -> new LoquiEnvelope("tar|get", "sender", "ping_message", "{}"))
                .isInstanceOf(LoquiEncodeException.class);
    }

    @Test
    void headerFieldsCanNotBeNullOrEmpty() {
        assertThatThrownBy(() -> new LoquiEnvelope(null, "sender", "ping_message", "{}"))
                .isInstanceOf(LoquiEncodeException.class);

        assertThatThrownBy(() -> new LoquiEnvelope("target", "sender", "", "{}"))
                .isInstanceOf(LoquiEncodeException.class);

        assertThatThrownBy(() -> new LoquiEnvelope("target", "sender", "ping_message", null))
                .isInstanceOf(LoquiEncodeException.class);
    }
}
