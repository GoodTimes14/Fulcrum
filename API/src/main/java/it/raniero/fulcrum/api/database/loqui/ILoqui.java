package it.raniero.fulcrum.api.database.loqui;

import io.lettuce.core.RedisFuture;
import it.raniero.fulcrum.api.database.loqui.discovery.ILoquiDiscovery;
import it.raniero.fulcrum.api.database.loqui.message.LoquiEnvelope;
import it.raniero.fulcrum.api.database.loqui.message.LoquiMessage;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Sender-receiver messaging layer built on top of Redis Pub/Sub.
 */
public interface ILoqui {

    /**
     * Target accepted by every listening instance.
     */
    String BROADCAST = "BROADCAST";

    /**
     * Gets the stable sender id used by this Loqui instance.
     *
     * @return local sender id
     */
    UUID getSenderId();

    /**
     * Gets the endpoint discovery API bound to this Loqui instance.
     *
     * @return Loqui discovery API
     */
    ILoquiDiscovery discovery();

    /**
     * Registers the decoder used to rebuild a message type received from a Loqui channel.
     *
     * @param messageClass message type to register
     * @param supplier     Supplier of the registering message type
     */
    void registerMessageType(Class<? extends LoquiMessage> messageClass, Supplier<LoquiMessage> supplier);

    /**
     * Subscribes to a channel and marks it as carrying Loqui envelopes.
     *
     * @param channel Redis channel
     */
    void registerChannel(String channel);

    /**
     * Gets the channels handled by Loqui.
     *
     * @return registered Loqui channels
     */
    Set<String> getChannels();

    /**
     * Registers a multicast group this instance belongs to on a channel.
     *
     * <p>Every message sent on that channel with the group as its target is then handled here, on top of the
     * ones addressed to this instance directly or to {@link #BROADCAST}. A channel can hold as many groups as
     * needed, and the same group can be registered on several channels.
     *
     * <p>The channel still has to be registered through {@link #registerChannel(String)} to be listened to.
     *
     * @param channel Redis channel the group applies to
     * @param group   multicast group to join
     */
    void registerMulticastGroup(String channel, String group);

    /**
     * Gets the multicast groups this instance belongs to on a channel.
     *
     * @param channel Redis channel
     * @return joined multicast groups, empty when none was registered for the channel
     */
    Set<String> getMulticastGroups(String channel);

    /**
     * Checks whether a message target must be handled by this instance on a channel.
     *
     * @param channel Redis channel the message was received on
     * @param target  target carried by the received envelope
     * @return {@code true} for this instance id, {@link #BROADCAST}, or a multicast group joined on the channel
     */
    boolean isTargeted(String channel, String target);

    /**
     * Decodes the message carried by an envelope.
     *
     * @param envelope received envelope
     * @return decoded message, or {@code null} when no decoder is registered for its packet id
     */
    LoquiMessage decodeMessage(LoquiEnvelope envelope);

    /**
     * Sends a message to a channel.
     *
     * @param channel Redis channel
     * @param target  receiver id, a multicast group, or {@link #BROADCAST}
     * @param message message to send
     */
    void sendMessage(String channel, String target, LoquiMessage message);

    /**
     * Sends a message to every instance listening on a channel.
     *
     * @param channel Redis channel
     * @param message message to send
     */
    default void broadcastMessage(String channel, LoquiMessage message) {
        sendMessage(channel, BROADCAST, message);
    }

    /**
     * Sends a message to a channel asynchronously.
     *
     * @param channel Redis channel
     * @param target  receiver id, a multicast group, or {@link #BROADCAST}
     * @param message message to send
     * @return future number of clients that received the message
     */
    RedisFuture<Long> sendMessageAsync(String channel, String target, LoquiMessage message);
}
