package it.raniero.fulcrum.database.redis.loqui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import it.raniero.fulcrum.api.database.loqui.ILoqui;
import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryOptions;
import it.raniero.fulcrum.api.database.loqui.exception.LoquiDecoderException;
import it.raniero.fulcrum.api.database.loqui.exception.LoquiEncodeException;
import it.raniero.fulcrum.api.database.loqui.message.LoquiContent;
import it.raniero.fulcrum.api.database.loqui.message.LoquiEnvelope;
import it.raniero.fulcrum.api.database.loqui.message.LoquiMessage;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FulcrumLoquiTest {

    private FulcrumLoqui loqui;

    @BeforeEach
    void setUp() {
        // The connection is only touched when publishing or subscribing.
        loqui = new FulcrumLoqui(null);
    }

    @Test
    void encodedMessageIsDecodedBackThroughTheRegisteredDecoder() {
        loqui.registerMessageType(PingMessage.class, PingMessage::new);

        LoquiEnvelope envelope = loqui.createEnvelope("target", new PingMessage("a|b", 7));
        LoquiMessage decoded = loqui.decodeMessage(LoquiEnvelope.deserialize(envelope.serialize()));

        assertThat(decoded).isInstanceOf(PingMessage.class);
        assertThat(((PingMessage) decoded).content).isEqualTo("a|b");
        assertThat(((PingMessage) decoded).count).isEqualTo(7);
    }

    @Test
    void packetIdIsDerivedFromTheClassNameInSnakeCase() {
        loqui.registerMessageType(PingMessage.class, PingMessage::new);

        assertThat(loqui.createEnvelope("target", new PingMessage("hi", 1)).packetId())
                .isEqualTo("ping_message");
    }

    @Test
    void envelopeIsSentFromTheLocalSenderId() {
        loqui.registerMessageType(PingMessage.class, PingMessage::new);

        assertThat(loqui.createEnvelope("target", new PingMessage("hi", 1)).from())
                .isEqualTo(FulcrumLoqui.SENDER_UUID.toString());
        assertThat(loqui.getSenderId()).isEqualTo(FulcrumLoqui.SENDER_UUID);
    }

    @Test
    void discoveryOptionsAreValidatedBeforeBeingApplied() {
        LoquiDiscoveryOptions valid = new LoquiDiscoveryOptions(10, 20, true);

        loqui.discovery().setOptions(valid);

        assertThat(loqui.discovery().getOptions()).isEqualTo(valid);
        assertThat(loqui.discovery().getSenderId()).isEqualTo(loqui.getSenderId());
        assertThatThrownBy(() -> loqui.discovery().setOptions(new LoquiDiscoveryOptions(10, 19, false)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(loqui.discovery().getOptions()).isEqualTo(valid);
    }

    @Test
    void registeringTheSamePacketIdTwiceFails() {
        loqui.registerMessageType(PingMessage.class, PingMessage::new);

        assertThatThrownBy(() -> loqui.registerMessageType(PingMessage.class, PingMessage::new))
                .isInstanceOf(LoquiDecoderException.class);
    }

    @Test
    void nullDecoderIsRejected() {
        assertThatThrownBy(() -> loqui.registerMessageType(PingMessage.class, null))
                .isInstanceOf(LoquiDecoderException.class);
    }

    @Test
    void encodingAnUnregisteredMessageFails() {
        assertThatThrownBy(() -> loqui.createEnvelope("target", new PingMessage("hi", 1)))
                .isInstanceOf(LoquiEncodeException.class)
                .hasMessageContaining("did you register it?");
    }

    @Test
    void decodingAnUnknownPacketIdReturnsNull() {
        assertThat(loqui.decodeMessage(new LoquiEnvelope("target", "sender", "unknown_message", "{}")))
                .isNull();
    }

    @Test
    void aFailingDecoderIsReportedAsADecoderException() {
        loqui.registerMessageType(PingMessage.class, () -> {
            throw new IllegalStateException("boom");
        });

        LoquiEnvelope envelope = loqui.createEnvelope("target", new PingMessage("hi", 1));

        assertThatThrownBy(() -> loqui.decodeMessage(envelope))
                .isInstanceOf(LoquiDecoderException.class)
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void aChannelHoldsSeveralMulticastGroups() {
        loqui.registerMulticastGroup("channel", "lobbies");
        loqui.registerMulticastGroup("channel", "eu-west");

        assertThat(loqui.getMulticastGroups("channel")).containsExactlyInAnyOrder("lobbies", "eu-west");
        assertThat(loqui.isTargeted("channel", "lobbies")).isTrue();
        assertThat(loqui.isTargeted("channel", "eu-west")).isTrue();
    }

    @Test
    void multicastGroupsAreScopedToTheirChannel() {
        loqui.registerMulticastGroup("channel", "lobbies");

        assertThat(loqui.isTargeted("other-channel", "lobbies")).isFalse();
        assertThat(loqui.getMulticastGroups("other-channel")).isEmpty();
    }

    @Test
    void theSameGroupCanBeJoinedOnSeveralChannels() {
        loqui.registerMulticastGroup("channel", "lobbies");
        loqui.registerMulticastGroup("other-channel", "lobbies");

        assertThat(loqui.isTargeted("channel", "lobbies")).isTrue();
        assertThat(loqui.isTargeted("other-channel", "lobbies")).isTrue();
    }

    @Test
    void joiningTheSameGroupTwiceIsIdempotent() {
        loqui.registerMulticastGroup("channel", "lobbies");
        loqui.registerMulticastGroup("channel", "lobbies");

        assertThat(loqui.getMulticastGroups("channel")).containsExactly("lobbies");
    }

    @Test
    void broadcastAndOwnIdAreAlwaysTargetedWhileUnknownTargetsAreNot() {
        assertThat(loqui.isTargeted("channel", ILoqui.BROADCAST)).isTrue();
        assertThat(loqui.isTargeted("channel", FulcrumLoqui.SENDER_UUID.toString()))
                .isTrue();
        assertThat(loqui.isTargeted("channel", UUID.randomUUID().toString())).isFalse();
        assertThat(loqui.isTargeted("channel", "lobbies")).isFalse();
        assertThat(loqui.isTargeted("channel", null)).isFalse();
    }

    @Test
    void returnedMulticastGroupsCanNotBeMutated() {
        loqui.registerMulticastGroup("channel", "lobbies");

        assertThatThrownBy(() -> loqui.getMulticastGroups("channel").add("sneaky"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void unusableMulticastGroupsAreRejected() {
        assertThatThrownBy(() -> loqui.registerMulticastGroup("channel", null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> loqui.registerMulticastGroup("channel", ""))
                .isInstanceOf(IllegalArgumentException.class);

        // The target travels in a header field, a separator would break the envelope.
        assertThatThrownBy(() -> loqui.registerMulticastGroup("channel", "lob|bies"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> loqui.registerMulticastGroup(null, "lobbies"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aMessageCanBeAddressedToAMulticastGroup() {
        loqui.registerMessageType(PingMessage.class, PingMessage::new);

        assertThat(loqui.createEnvelope("lobbies", new PingMessage("hi", 1)).target())
                .isEqualTo("lobbies");
    }

    static class PingMessage extends LoquiMessage {

        String content;

        int count;

        PingMessage(String content, int count) {
            this.content = content;
            this.count = count;
        }

        public PingMessage() {
            super();
        }

        @Override
        public void deserialize(LoquiContent content) {
            this.content = content.getString("content");
            this.count = content.getInt("count", 0);
        }
    }
}
