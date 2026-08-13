package it.raniero.fulcrum.database.redis.loqui;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.lettuce.core.RedisFuture;
import it.raniero.fulcrum.api.database.loqui.ILoqui;
import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryOptions;
import it.raniero.fulcrum.api.database.loqui.exception.LoquiDecoderException;
import it.raniero.fulcrum.api.database.loqui.exception.LoquiEncodeException;
import it.raniero.fulcrum.api.database.loqui.message.LoquiContent;
import it.raniero.fulcrum.api.database.loqui.message.LoquiEnvelope;
import it.raniero.fulcrum.api.database.loqui.message.LoquiMessage;
import it.raniero.fulcrum.database.redis.LettuceConnection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FulcrumLoqui implements ILoqui {

    public static final UUID SENDER_UUID = UUID.randomUUID();

    private final LettuceConnection connection;

    @Getter
    private final Set<String> channels = ConcurrentHashMap.newKeySet();

    private final Map<String, Class<? extends LoquiMessage>> packetMap = new ConcurrentHashMap<>();

    private final Map<String, Supplier<LoquiMessage>> decoderMap = new ConcurrentHashMap<>();

    private final Map<String, String> classCache = new ConcurrentHashMap<>();

    private LoquiDiscoveryOptions discoveryOptions = LoquiDiscoveryOptions.defaultOptions();

    private final Map<String, Set<String>> multicastGroups = new ConcurrentHashMap<>();

    private static final TypeReference<Map<String, Object>> RAW_INPUT_TYPE = new TypeReference<>() {};

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            // Messages are plain data holders, so read their fields directly instead of requiring getters.
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Override
    public void registerMessageType(Class<? extends LoquiMessage> messageClass, Supplier<LoquiMessage> supplier) {
        if (messageClass == null || supplier == null) {
            throw new LoquiDecoderException("Message class and decoder can't be null!");
        }

        String sanitizedPacketId = sanitizePacketId(messageClass.getSimpleName());
        if (sanitizedPacketId.isEmpty()) {
            throw new LoquiDecoderException(
                    "Can't derive a packet id from class: " + messageClass.getName() + ", use a named class!");
        }

        Class<? extends LoquiMessage> previous = packetMap.putIfAbsent(sanitizedPacketId, messageClass);
        if (previous != null) {
            throw new LoquiDecoderException("Decoder with id (" + messageClass.getSimpleName() + " sanitized: "
                    + sanitizedPacketId + ") already registered by " + previous.getName() + "!");
        }

        decoderMap.put(sanitizedPacketId, supplier);
        classCache.put(messageClass.getName(), sanitizedPacketId);
    }

    @Override
    public void registerChannel(String channel) {
        if (channel == null || channel.isEmpty()) {
            throw new IllegalArgumentException("Loqui channel can't be null or empty!");
        }

        // Marked before subscribing, otherwise a message arriving in between is handled as a raw one.
        if (!channels.add(channel)) return;

        connection.subscribe(channel);
    }

    @Override
    public void registerMulticastGroup(String channel, String group) {
        if (channel == null || channel.isEmpty()) {
            throw new IllegalArgumentException("Loqui channel can't be null or empty!");
        }

        if (!LoquiEnvelope.isValidHeaderField(group)) {
            throw new IllegalArgumentException("Multicast group can't be null, empty or hold a separator: " + group);
        }

        multicastGroups
                .computeIfAbsent(channel, key -> ConcurrentHashMap.newKeySet())
                .add(group);
    }

    @Override
    public Set<String> getMulticastGroups(String channel) {
        Set<String> groups = multicastGroups.get(channel);

        return groups == null ? Set.of() : Collections.unmodifiableSet(groups);
    }

    @Override
    public void setDiscoveryOptions(LoquiDiscoveryOptions options) {
        this.discoveryOptions = options;
    }

    @Override
    public LoquiDiscoveryOptions getDiscoveryOptions() {
        return this.discoveryOptions;
    }

    @Override
    public boolean isTargeted(String channel, String target) {
        if (target == null) return false;

        return BROADCAST.equals(target)
                || SENDER_UUID.toString().equals(target)
                || getMulticastGroups(channel).contains(target);
    }

    @Override
    public LoquiMessage decodeMessage(LoquiEnvelope envelope) {

        Supplier<LoquiMessage> decoder = decoderMap.get(envelope.packetId());
        if (decoder == null) return null;

        Map<String, Object> rawInput;
        try {

            rawInput = MAPPER.readValue(envelope.serializedMessage(), RAW_INPUT_TYPE);

        } catch (JsonProcessingException e) {
            throw new LoquiDecoderException(
                    "Can't decode Loqui message for packet id (" + envelope.packetId() + "): " + e.getOriginalMessage(),
                    e);
        }

        try {

            LoquiMessage message = decoder.get();
            message.deserialize(new LoquiContent(rawInput));

            return message;

        } catch (Exception e) {
            throw new LoquiDecoderException(
                    "Decoder failed for packet id (" + envelope.packetId() + "): " + e.getMessage(), e);
        }
    }

    @Override
    public void sendMessage(String channel, String target, LoquiMessage message) {
        LoquiEnvelope envelope = createEnvelope(target, message);

        try {

            connection.publish(channel, envelope.serialize());

        } catch (Exception e) {
            throw new LoquiEncodeException("Can't send Loqui message: " + e.getMessage(), e);
        }
    }

    @Override
    public RedisFuture<Long> sendMessageAsync(String channel, String target, LoquiMessage message) {
        LoquiEnvelope envelope = createEnvelope(target, message);

        try {

            return connection.publishAsync(channel, envelope.serialize());

        } catch (Exception e) {
            throw new LoquiEncodeException("Can't send Loqui message: " + e.getMessage(), e);
        }
    }

    public LoquiEnvelope createEnvelope(String target, LoquiMessage message) {
        if (message == null) {
            throw new LoquiEncodeException("Can't encode a null Loqui message!");
        }

        String packetId = classCache.get(message.getClass().getName());
        if (packetId == null) {
            throw new LoquiEncodeException(
                    "packetId not found for class: " + message.getClass().getName() + ", did you register it?");
        }

        String serialized;
        try {

            serialized = MAPPER.writeValueAsString(message);

        } catch (JsonProcessingException e) {
            throw new LoquiEncodeException(
                    "Can't encode Loqui message: " + message.getClass().getSimpleName() + " " + e.getOriginalMessage(),
                    e);
        }

        return new LoquiEnvelope(target, SENDER_UUID.toString(), packetId, serialized);
    }

    private String sanitizePacketId(String packetId) {
        return packetId.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replace(' ', '_')
                .toLowerCase(Locale.ROOT);
    }
}
