package it.raniero.fulcrum.database.redis.loqui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisFuture;
import it.raniero.fulcrum.api.database.loqui.ILoqui;
import it.raniero.fulcrum.api.database.loqui.exception.LoquiDecoderException;
import it.raniero.fulcrum.api.database.loqui.exception.LoquiEncodeException;
import it.raniero.fulcrum.api.database.loqui.message.LoquiEnvelope;
import it.raniero.fulcrum.api.database.loqui.message.LoquiMessage;
import it.raniero.fulcrum.database.redis.LettuceConnection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@RequiredArgsConstructor
public class FulcrumLoqui implements ILoqui {

    private final LettuceConnection connection;

    @Getter
    private final Set<String> channels = ConcurrentHashMap.newKeySet();

    private final Map<String, Class<? extends LoquiMessage>> packetMap = new ConcurrentHashMap<>();

    private final Map<String, String> classCache = new ConcurrentHashMap<>();

    private static final ObjectMapper MAPPER = new ObjectMapper();


    @Override
    public void registerMessageDecoder(Class<? extends LoquiMessage> messageClass, Function<Map<String, String>, LoquiMessage> decoder) {
        String sanitizedPacketId = sanitizePacketId(messageClass.getSimpleName());
        if (packetMap.containsKey(sanitizedPacketId)) {
            throw new LoquiDecoderException("Decoder with id (" + messageClass.getSimpleName() + " sanitized: " + sanitizedPacketId + ") already registered!");
        }

        packetMap.put(sanitizedPacketId, messageClass);
        classCache.put(messageClass.getSimpleName(), sanitizedPacketId);
    }


    @Override
    public void registerChannel(String channel) {
        connection.subscribe(channel);
        channels.add(channel);
    }

    @Override
    public LoquiMessage decodeMessage(LoquiEnvelope envelope) {


        Class<? extends LoquiMessage> clazz = packetMap.get(envelope.packetId());
        if (clazz == null) return null;

        try {

            return MAPPER.readValue(envelope.serializedMessage(), clazz);

        } catch (JsonProcessingException e) {
            throw new LoquiDecoderException(
                    "Can't decode Loqui message for packet id (" + envelope.target() + "): " + e.getOriginalMessage());
        }
    }

    @Override
    public void sendMessage(String channel, String target, LoquiMessage message) {
        try {

            connection.publish(channel, createEnvelope(target, message).serialize());

        } catch (Exception e) {
            throw new LoquiEncodeException("Can't send Loqui message: " + e.getMessage());
        }
    }

    @Override
    public RedisFuture<Long> sendMessageAsync(String channel, String target, LoquiMessage message) {
        try {

            return connection.publishAsync(channel, createEnvelope(target, message).serialize());

        } catch (Exception e) {
            throw new LoquiEncodeException("Can't send Loqui message: " + e.getMessage());
        }
    }


    public LoquiEnvelope createEnvelope(String target, LoquiMessage message) {
        try {

            String serialized = MAPPER.writeValueAsString(message);
            String packetId = classCache.get(message.getClass().getSimpleName());
            if (packetId == null) {
                throw new LoquiEncodeException("packetId not found for class: " + message.getClass().getSimpleName() + ", did you register it?");
            }

            return new LoquiEnvelope(target, packetId, serialized);

        } catch (Exception e) {
            throw new LoquiEncodeException("Can't encode Loqui message: " + message.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    private String sanitizePacketId(String packetId) {
        return packetId
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replace(' ', '_')
                .toLowerCase(Locale.ROOT);
    }

}
